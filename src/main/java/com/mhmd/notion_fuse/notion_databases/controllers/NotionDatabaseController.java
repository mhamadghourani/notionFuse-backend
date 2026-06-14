package com.mhmd.notion_fuse.notion_databases.controllers;

import com.mhmd.notion_fuse.notion_databases.model.MergeHistoryDto;
import com.mhmd.notion_fuse.notion_databases.model.MergeRequestDto;
import com.mhmd.notion_fuse.notion_databases.model.NotionDatabaseDto;
import com.mhmd.notion_fuse.notion_databases.model.SyncedDatabaseDto;
import com.mhmd.notion_fuse.notion_databases.services.NotionDatabaseService;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notion")
@RequiredArgsConstructor
public class NotionDatabaseController {

    private final NotionDatabaseService notionDatabaseService;
    private final UserRepository userRepository;

    @GetMapping("/my-databases")
    public ResponseEntity<List<NotionDatabaseDto>> getMyDatabases(Authentication authentication) {

        User user = resolveUser(authentication);

        List<NotionDatabaseDto> databases =
                notionDatabaseService.getUserDatabases(user.getId());

        return ResponseEntity.ok(databases);
    }

    @PostMapping("/merge")
    public ResponseEntity<String> mergeDatabases(
            Authentication authentication,
            @RequestBody MergeRequestDto requestDto
    ) {

        log.info("INCOMING MERGE REQUEST PAYLOAD: {}", requestDto);

        User user = resolveUser(authentication);

        notionDatabaseService.initiateDatabaseMerge(
                user.getId(),
                requestDto,
                user
        );

        return ResponseEntity.ok("Database merge completed successfully!");
    }

    @GetMapping("/pipelines")
    public ResponseEntity<List<SyncedDatabaseDto>> getActivePipelines(
            Authentication authentication
    ) {

        User user = resolveUser(authentication);

        List<SyncedDatabaseDto> pipelines =
                notionDatabaseService.getActivePipelinesByUserId(user.getId());

        return ResponseEntity.ok(pipelines);
    }

    @PostMapping("/sync/{syncId}")
    public ResponseEntity<String> syncDatabase(
            Authentication authentication,
            @PathVariable Long syncId
    ) {

        User user = resolveUser(authentication);

        notionDatabaseService.syncExistingMerge(syncId, user.getId());

        return ResponseEntity.ok("Synchronization complete!");
    }

    @GetMapping("/history")
    public ResponseEntity<List<MergeHistoryDto>> getHistory(
            Authentication authentication
    ) {

        User user = resolveUser(authentication);

        return ResponseEntity.ok(
                notionDatabaseService.getHistoryByEmail(user.getEmail())
        );
    }

    private User resolveUser(Authentication authentication) {

        if (authentication == null) {
            log.error("Authentication object is NULL");
            throw new RuntimeException("User is not authenticated");
        }

        String email = authentication.getName();

        if (email == null || email.isBlank()) {
            log.error("Authentication name is NULL");
            throw new RuntimeException("Authenticated user email not found");
        }

        log.info("Authenticated user email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User profile not found for email: " + email
                        )
                );
    }
}