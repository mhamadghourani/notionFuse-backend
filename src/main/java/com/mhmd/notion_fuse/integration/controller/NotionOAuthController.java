package com.mhmd.notion_fuse.integration.controller;

import com.mhmd.notion_fuse.integration.service.NotionOAuthService;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Slf4j
@Controller // Switched from @RestController to support clean browser navigation redirects
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class NotionOAuthController {

    private final NotionOAuthService oauthService;
    private final UserRepository userRepository;

    @Value("${NOTION_CLIENT_ID}")
    private String clientId;

    @Value("${NOTION_REDIRECT_URI}")
    private String redirectUri;

    /**
     * Endpoint 1: Call this from the frontend to get the dynamic link containing the current user's ID
     */
    @GetMapping("/authorize-url")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getNotionAuthorizeUrl(Authentication authentication) {
        if (authentication == null) {
            log.warn("Authorization link request denied: User is not authenticated in SecurityContext.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Get user email directly from the verified JWT filter context
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User profile not found for email: " + email));

            // Dynamically build the exact state link (e.g., state=user_1)
            String dynamicUrl = String.format(
                    "https://api.notion.com/v1/oauth/authorize?owner=user&client_id=%s&response_type=code&redirect_uri=%s&state=user_%d",
                    clientId,
                    redirectUri,
                    currentUser.getId()
            );

            log.info("Successfully generated secure Notion state login link for User ID: {}", currentUser.getId());
            return ResponseEntity.ok(Map.of("url", dynamicUrl));
        } catch (Exception e) {
            log.error("Failed to build dynamic Notion oauth link", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint 2: Your existing callback method that handles the Notion code and states bounce
     */
    @GetMapping("/callback/notion")
    public String handleNotionCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state) {

        log.info("Notion callback received without session. Extracting User from State: {}", state);

        // Resolves user purely from the incoming state ("user_1")
        User targetUser = resolveUserFromState(state);

        if (targetUser == null) {
            log.warn("OAuth callback failed: User ID extracted from state does not exist in Database.");
            return "redirect:http://localhost:3000/connections?status=error";
        }

        try {
            oauthService.saveNotionIntegration(code, targetUser);
            log.info("Integration completely successful for test user: {}", targetUser.getEmail());

            // Clean redirect straight back to our frontend page with parameters
            return "redirect:http://localhost:3000/connections?status=success";
        } catch (Exception e) {
            log.error("Failed to save Notion integration", e);
            return "redirect:http://localhost:3000/connections?status=error";
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