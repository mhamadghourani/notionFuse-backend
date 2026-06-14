package com.mhmd.notion_fuse.integration.service;

import com.mhmd.notion_fuse.integration.constant.Platform;
import com.mhmd.notion_fuse.integration.model.Integration;
import com.mhmd.notion_fuse.integration.repository.IntegrationRepository;
import com.mhmd.notion_fuse.user.entity.User;
import com.mhmd.notion_fuse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotionOAuthService {

    private final RestClient notionRestClient;
    private final IntegrationRepository integrationRepository;
    private final UserRepository userRepository;

    @Value("${NOTION_CLIENT_ID}")
    private String clientId;

    @Value("${NOTION_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${NOTION_REDIRECT_URI}")
    private String redirectUri;

    @Transactional
    public void saveNotionIntegration(String code, User user) {
        // 1. Fetch the user directly from the DB context to guarantee it's not detached
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + user.getId()));

        System.out.println("🔥 [OAUTH START] Authenticated User ID: " + managedUser.getId() + " | Email: " + managedUser.getEmail());

        String credentials = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        Map<String, Object> response = notionRestClient.post()
                .uri("/oauth/token")
                .header("Authorization", "Basic " + credentials)
                .body(Map.of(
                        "grant_type", "authorization_code",
                        "code", code,
                        "redirect_uri", redirectUri
                ))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("Could not retrieve token from Notion");
        }

        // 2. Clear any old integrations for this user to avoid duplication issues
        integrationRepository.findByUserIdAndPlatform(managedUser.getId(), Platform.NOTION)
                .ifPresent(old -> {
                    System.out.println("🔥 Removing old mismatched integration row ID: " + old.getId());
                    integrationRepository.delete(old);
                    integrationRepository.flush();
                });

        // 3. Build a completely fresh, explicit integration linked to the database user
        Integration integration = new Integration();
        integration.setUser(managedUser);
        integration.setPlatform(Platform.NOTION);
        integration.setAccessToken((String) response.get("access_token"));

        Map<String, Object> workspace = (Map<String, Object>) response.get("workspace");
        if (workspace != null) {
            integration.setWorkSpaceId((String) workspace.get("id"));
            integration.setWorkSpaceName((String) workspace.get("name"));
        } else {
            integration.setWorkSpaceId((String) response.get("workspace_id"));
            integration.setWorkSpaceName((String) response.get("workspace_name"));
        }
        integration.setBotId((String) response.get("bot_id"));

        Integration saved = integrationRepository.saveAndFlush(integration);
        System.out.println("🔥 [OAUTH SUCCESS] Saved Integration ID: " + saved.getId() + " strictly tied to User ID: " + saved.getUser().getId());
    }
}