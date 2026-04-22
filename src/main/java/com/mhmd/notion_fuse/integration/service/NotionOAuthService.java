package com.mhmd.notion_fuse.integration.service;

import com.mhmd.notion_fuse.integration.constant.Platform;
import com.mhmd.notion_fuse.integration.model.Integration;
import com.mhmd.notion_fuse.integration.repository.IntegrationRepository;
import com.mhmd.notion_fuse.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotionOAuthService {

    private final RestClient notionRestClient;
    private final IntegrationRepository integrationRepository;

    @Value("${NOTION_CLIENT_ID}")
    private String clientId;
    @Value("${NOTION_CLIENT_SECRET}")
    private String clientSecret;
    @Value("${NOTION_REDIRECT_URI}")
    private String redirectUri;

    public void saveNotionIntegration(String code, User user) {
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
            throw new RuntimeException("Could not retrieve token from Notion. Please check your credentials.");
        }

        Integration integration = integrationRepository
                .findByUserIdAndPlatform(user.getId(), Platform.NOTION)
                .orElse(new Integration());

        integration.setUser(user);
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

        integrationRepository.save(integration);
    }
}