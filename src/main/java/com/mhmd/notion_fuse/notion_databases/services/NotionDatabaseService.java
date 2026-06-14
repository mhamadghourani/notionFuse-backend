package com.mhmd.notion_fuse.notion_databases.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhmd.notion_fuse.clients.notion.NotionClient;
import com.mhmd.notion_fuse.integration.constant.Platform;
import com.mhmd.notion_fuse.integration.model.Integration;
import com.mhmd.notion_fuse.integration.repository.IntegrationRepository;
import com.mhmd.notion_fuse.exceptions.InvalidNotionParentException;
import com.mhmd.notion_fuse.notion_databases.entity.MergeHistoryEntity;
import com.mhmd.notion_fuse.notion_databases.entity.SyncedDatabase;
import com.mhmd.notion_fuse.notion_databases.mappers.NotionDataMapper;
import com.mhmd.notion_fuse.notion_databases.mappers.NotionRowEngine;
import com.mhmd.notion_fuse.notion_databases.mappers.NotionSchemaEngine;
import com.mhmd.notion_fuse.notion_databases.mappers.SyncedPageMapping;
import com.mhmd.notion_fuse.notion_databases.model.*;
import com.mhmd.notion_fuse.notion_databases.repository.MergeHistoryRepository;
import com.mhmd.notion_fuse.notion_databases.repository.SyncedDatabaseRepository;
import com.mhmd.notion_fuse.notion_databases.repository.SyncedPageMappingRepository;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotionDatabaseService {

    private final NotionClient notionClient;
    private final IntegrationRepository integrationRepository;
    private final NotionDataMapper notionDataMapper;
    private final SyncedDatabaseRepository syncedDatabaseRepository;
    private final SyncedPageMappingRepository syncedPageMappingRepository;
    private final UserRepository userRepository;
    private final MergeHistoryRepository mergeHistoryRepository;
    private final ObjectMapper objectMapper;
    private final NotionSchemaEngine notionSchemaEngine;
    private final NotionRowEngine notionRowEngine;

    // =========================
    // CALL DATABASES
    // =========================
    public List<NotionDatabaseDto> getUserDatabases(Long userId) {
        System.out.println("🔍 [FETCH START] Controller requested databases for User ID: " + userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found in database for ID: " + userId));

        System.out.println("🔍 [USER FOUND] Found user " + user.getEmail() + " in database. Querying integration next...");

        Integration integration = integrationRepository
                .findByUserIdAndPlatform(userId, Platform.NOTION)
                .orElseThrow(() -> {
                    System.out.println("❌ [FETCH FAIL] Database returned NO integration rows for User ID: " + userId);
                    return new RuntimeException("Notion is not connected");
                });

        System.out.println("✅ [FETCH SUCCESS] Integration row found! Access Token: " + integration.getAccessToken().substring(0, 10) + "...");

        return notionDataMapper.toDatabaseDtoList(
                notionClient.listDataBasesAsRawJson(integration.getAccessToken())
        );
    }

    // =========================
    // MERGE DATABASES
    // =========================
    public void initiateDatabaseMerge(Long userId, MergeRequestDto requestDto, User userEntity) {

        Integration integration = integrationRepository
                .findByUserIdAndPlatform(userId, Platform.NOTION)
                .orElseThrow(() -> new RuntimeException("Notion is not connected"));

        String token = integration.getAccessToken();

        MergeHistoryEntity history = new MergeHistoryEntity();
        history.setUserId(userId);
        history.setDatabaseName(requestDto.customMergedDatabaseName());
        history.setStatus("PENDING");
        history.setCreatedAt(LocalDateTime.now());
        mergeHistoryRepository.save(history);

        try {
            String rawSchemaA = notionClient.getDatabase(requestDto.sourceDatabaseAId(), token);
            String rawSchemaB = notionClient.getDatabase(requestDto.sourceDatabaseBId(), token);

            String parentId = requestDto.targetParentPageId();
            if (parentId == null || parentId.isBlank()) {
                parentId = notionDataMapper.extractParentIdFromSchema(rawSchemaA);
            }

            if (parentId == null) {
                throw new InvalidNotionParentException(
                        "Could not extract a valid target page. Please ensure your databases are on a main page layout body."
                );
            }

            String primaryTitle = notionSchemaEngine.getPrimaryTitleName(rawSchemaA);
            Map<String, Object> mergedSchema = notionSchemaEngine.mergeSchemas(rawSchemaA, rawSchemaB);

            String response = notionClient.createMergedDatabase(
                    token,
                    parentId.replace("-", ""),
                    requestDto.customMergedDatabaseName(),
                    mergedSchema
            );

            String destId = notionDataMapper.extractDatabaseId(response);

            SyncedDatabase sync = new SyncedDatabase();
            sync.setUser(userEntity);
            sync.setSourceDatabaseAId(requestDto.sourceDatabaseAId());
            sync.setSourceDatabaseBId(requestDto.sourceDatabaseBId());
            sync.setDestinationDatabaseId(destId);
            sync.setDatabaseName(requestDto.customMergedDatabaseName());
            sync.setAutomationActive(true);

            var saved = syncedDatabaseRepository.save(sync);

            processAllPages(requestDto.sourceDatabaseAId(), token, "A", primaryTitle, destId, saved.getId());
            processAllPages(requestDto.sourceDatabaseBId(), token, "B", primaryTitle, destId, saved.getId());

            history.setStatus("SUCCESS");
            mergeHistoryRepository.save(history);

        } catch (InvalidNotionParentException e) {
            // Keep status tracking updated cleanly
            history.setStatus("FAILED");
            mergeHistoryRepository.save(history);
            throw e; // Throw completely untouched so the controller advice catches it directly
        } catch (Exception e) {
            history.setStatus("FAILED");
            mergeHistoryRepository.save(history);
            throw new RuntimeException("Merge failed: " + e.getMessage(), e);
        }
    }

    // =========================
    // SYNC
    // =========================
    public void syncExistingMerge(Long syncId, Long userId) {

        Integration integration = integrationRepository
                .findByUserIdAndPlatform(userId, Platform.NOTION)
                .orElseThrow(() -> new RuntimeException("Notion is not connected"));

        SyncedDatabase sync = syncedDatabaseRepository.findById(syncId)
                .orElseThrow(() -> new RuntimeException("Sync not found"));

        String token = integration.getAccessToken();

        String primaryTitle = notionSchemaEngine.getPrimaryTitleName(
                notionClient.getDatabase(sync.getSourceDatabaseAId(), token)
        );

        processAllPages(sync.getSourceDatabaseAId(), token, "A",
                primaryTitle, sync.getDestinationDatabaseId(), sync.getId());

        processAllPages(sync.getSourceDatabaseBId(), token, "B",
                primaryTitle, sync.getDestinationDatabaseId(), sync.getId());
    }

    // =========================
    // CORE SYNC
    // =========================
    private void processAllPages(
            String dbId,
            String token,
            String source,
            String primaryTitle,
            String destId,
            Long syncId
    ) {
        String cursor = null;
        boolean hasMore = true;

        while (hasMore) {
            String raw = notionClient.queryDatabase(dbId, token, cursor);

            try {
                JsonNode root = objectMapper.readTree(raw);
                JsonNode results = root.path("results");

                var rows = notionRowEngine.extractRawRows(raw);

                for (int i = 0; i < rows.size(); i++) {
                    String sourcePageId = results.get(i).path("id").asText();

                    Map<String, Object> data =
                            notionRowEngine.prepareForInsert(rows.get(i), source, primaryTitle);

                    Optional<SyncedPageMapping> mapping =
                            syncedPageMappingRepository.findBySyncConfigIdAndSourcePageId(syncId, sourcePageId);

                    if (mapping.isPresent()) {
                        notionClient.updateDynamicRow(token, mapping.get().getDestinationPageId(), data);
                    } else {
                        String res = notionClient.insertDynamicRow(token, destId, data);
                        String newId = objectMapper.readTree(res).path("id").asText();

                        syncedPageMappingRepository.save(
                                new SyncedPageMapping(null, syncId, sourcePageId, newId)
                        );
                    }
                }

                hasMore = root.path("has_more").asBoolean();
                cursor = hasMore ? root.path("next_cursor").asText() : null;

            } catch (Exception e) {
                hasMore = false;
                System.err.println("Sync error: " + e.getMessage());
            }
        }
    }

    // =========================
    // HISTORY
    // =========================
    public List<MergeHistoryDto> getHistoryByEmail(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return mergeHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(e -> new MergeHistoryDto(
                        e.getId().toString(),
                        e.getDatabaseName(),
                        e.getStatus(),
                        e.getCreatedAt() != null ? e.getCreatedAt().toString() : "UNKNOWN",
                        e.getNotionUrl()
                ))
                .toList();
    }

    // =========================
    // PIPELINES
    // =========================
    public List<SyncedDatabaseDto> getActivePipelinesByUserId(Long userId) {
        return syncedDatabaseRepository.findByUserId(userId).stream()
                .map(e -> SyncedDatabaseDto.builder()
                        .id(e.getId())
                        .databaseName(e.getDatabaseName())
                        .isAutomationActive(e.isAutomationActive())
                        .status(e.isAutomationActive() ? "ACTIVE" : "PAUSED")
                        .createdAt(e.getCreatedAt())
                        .build())
                .toList();
    }
}