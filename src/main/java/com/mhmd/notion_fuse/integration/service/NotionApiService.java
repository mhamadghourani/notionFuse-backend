package com.mhmd.notion_fuse.integration.service;

import com.mhmd.notion_fuse.clients.notion.NotionClient;
import com.mhmd.notion_fuse.integration.constant.Platform;
import com.mhmd.notion_fuse.integration.repository.IntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotionApiService {

    private final NotionClient notionClient;
    private final IntegrationRepository integrationRepository;

    public Map<String, Object> getUserDataBases(Long userId){
        var integration = integrationRepository.findByUserIdAndPlatform(userId, Platform.NOTION)
                .orElseThrow(()-> new RuntimeException("Notion is not connected"));
        return notionClient.listDataBases(integration.getAccessToken());
    }
}
