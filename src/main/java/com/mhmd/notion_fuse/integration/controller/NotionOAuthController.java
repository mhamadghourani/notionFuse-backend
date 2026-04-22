package com.mhmd.notion_fuse.integration.controller;

import com.mhmd.notion_fuse.integration.service.NotionOAuthService;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class NotionOAuthController {

    private final NotionOAuthService oauthService;
    private final UserRepository userRepository;

    @GetMapping("/callback/notion")
    public ResponseEntity<String> handleNotionCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            @AuthenticationPrincipal User user) {

        log.info("Notion callback received. State: {}", state);


        User targetUser = (user != null) ? user : resolveUserFromState(state);

        if (targetUser == null) {
            log.warn("OAuth callback failed: Could not identify user.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session or state.");
        }

        try {
            oauthService.saveNotionIntegration(code, targetUser);
            return ResponseEntity.ok("Integration successful for: " + targetUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to save Notion integration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Integration failed.");
        }
    }

    private User resolveUserFromState(String state) {
        if (state == null) return null;
        try {
            String userIdStr = state.startsWith("user_") ? state.replace("user_", "") : state;
            return userRepository.findById(Long.valueOf(userIdStr)).orElse(null);
        } catch (NumberFormatException e) {
            log.error("Invalid state format received: {}", state);
            return null;
        }
    }
}