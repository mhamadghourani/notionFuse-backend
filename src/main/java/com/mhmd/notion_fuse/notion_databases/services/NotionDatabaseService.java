package com.mhmd.notion_fuse.notion_databases.services;

import com.mhmd.notion_fuse.clients.notion.NotionClient;
import com.mhmd.notion_fuse.integration.constant.Platform;
import com.mhmd.notion_fuse.integration.repository.IntegrationRepository;
import com.mhmd.notion_fuse.notion_databases.entity.SyncedDatabase;
import com.mhmd.notion_fuse.notion_databases.mappers.NotionDataMapper;
import com.mhmd.notion_fuse.notion_databases.mappers.NotionRowEngine;
import com.mhmd.notion_fuse.notion_databases.mappers.NotionSchemaEngine;
import com.mhmd.notion_fuse.notion_databases.model.MergeRequestDto;
import com.mhmd.notion_fuse.notion_databases.model.NotionDatabaseDto;
import com.mhmd.notion_fuse.notion_databases.repository.SyncedDatabaseRepository;
import com.mhmd.notion_fuse.user.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class NotionDatabaseService {

    private final NotionClient notionClient;
    private final IntegrationRepository integrationRepository;
    private final NotionDataMapper notionDataMapper;
    private final SyncedDatabaseRepository syncedDatabaseRepository;
    private final NotionRowEngine notionRowEngine;
    private final NotionSchemaEngine notionSchemaEngine;

    public List<NotionDatabaseDto> getUserDatabases(Long userId) {
        var integration = integrationRepository.findByUserIdAndPlatform(userId, Platform.NOTION)
                .orElseThrow(() -> new RuntimeException("Notion is not connected"));
        return notionDataMapper.toDatabaseDtoList(notionClient.listDataBasesAsRawJson(integration.getAccessToken()));
    }

    @Async
    public void initiateDatabaseMerge(Long userId, MergeRequestDto requestDto, User userEntity) {
        var integration = integrationRepository.findByUserIdAndPlatform(userId, Platform.NOTION)
                .orElseThrow(() -> new RuntimeException("Notion is not connected"));
        String token = integration.getAccessToken();


        String rawSchemaA = notionClient.getDatabase(requestDto.sourceDatabaseAId(), token);
        String rawSchemaB = notionClient.getDatabase(requestDto.sourceDatabaseBId(), token);

        Map<String, Object> combinedSchema = notionSchemaEngine.mergeSchemas(rawSchemaA, rawSchemaB);
        String primaryTitleName = notionSchemaEngine.getPrimaryTitleName(rawSchemaA);


        String rawCreationResponse = notionClient.createMergedDatabase(
                token,
                requestDto.targetParentPageId(),
                requestDto.customMergedDatabaseName(),
                combinedSchema
        );
        String generatedDestinationId = notionDataMapper.extractDatabaseId(rawCreationResponse);


        processAllPages(requestDto.sourceDatabaseAId(), token, "Database A", primaryTitleName, generatedDestinationId);
        processAllPages(requestDto.sourceDatabaseBId(), token, "Database B", primaryTitleName, generatedDestinationId);


        SyncedDatabase syncConfig = new SyncedDatabase();
        syncConfig.setUser(userEntity);
        syncConfig.setSourceDatabaseAId(requestDto.sourceDatabaseAId());
        syncConfig.setSourceDatabaseBId(requestDto.sourceDatabaseBId());
        syncConfig.setDestinationDatabaseId(generatedDestinationId);
        syncConfig.setAutomationActive(true);
        syncedDatabaseRepository.save(syncConfig);

        System.out.println("🚀 Merge complete and synced to local repository!");
    }


    private void processAllPages(String dbId, String token, String sourceName, String primaryTitle, String destId) {
        String cursor = null;
        boolean hasMore = true;

        while (hasMore) {
            String rawResponse = notionClient.queryDatabase(dbId, token, cursor);
            List<JsonNode> rows = notionRowEngine.extractRawRows(rawResponse);

            for (JsonNode row : rows) {
                Map<String, Object> cleanData = notionRowEngine.prepareForInsert(row, sourceName, primaryTitle);


                String debugTitle = (cleanData.containsKey("Name")) ? cleanData.get("Name").toString() : "UNKNOWN";
                System.out.println("DEBUG: [" + sourceName + "] Attempting to insert: " + debugTitle);

                notionClient.insertDynamicRow(token, destId, cleanData);
            }


            try {
                com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(rawResponse);
                hasMore = root.path("has_more").asBoolean();
                cursor = hasMore ? root.path("next_cursor").asText() : null;
            } catch (Exception e) {
                hasMore = false;
            }
        }
    }
}