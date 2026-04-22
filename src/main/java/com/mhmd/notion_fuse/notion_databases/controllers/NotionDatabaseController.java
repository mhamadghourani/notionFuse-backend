package com.mhmd.notion_fuse.notion_databases.controllers;

import com.mhmd.notion_fuse.notion_databases.model.MergeRequestDto;
import com.mhmd.notion_fuse.notion_databases.model.NotionDatabaseDto;
import com.mhmd.notion_fuse.notion_databases.services.NotionDatabaseService;
import com.mhmd.notion_fuse.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notion")
@RequiredArgsConstructor
public class NotionDatabaseController {

    private final NotionDatabaseService notionDatabaseService;

    @GetMapping("/my-databases")
    public ResponseEntity<List<NotionDatabaseDto>> getMyDatabases(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(notionDatabaseService.getUserDatabases(user.getId()));
    }

    @PostMapping("/merge")
    public ResponseEntity<String> mergeDatabases(
            @AuthenticationPrincipal User user,
            @RequestBody MergeRequestDto requestDto
    ) {
        if (user == null) {
            log.warn("Unauthorized merge attempt blocked.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        notionDatabaseService.initiateDatabaseMerge(user.getId(), requestDto, user);
        return ResponseEntity.ok("Database merge initiated successfully!");
    }
}
