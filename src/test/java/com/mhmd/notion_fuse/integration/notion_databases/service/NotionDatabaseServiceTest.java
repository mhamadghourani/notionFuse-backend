package com.mhmd.notion_fuse.integration.notion_databases.service;

import com.mhmd.notion_fuse.clients.notion.NotionClient;
import com.mhmd.notion_fuse.integration.repository.IntegrationRepository;
import com.mhmd.notion_fuse.integration.constant.Platform;


import com.mhmd.notion_fuse.notion_databases.mappers.NotionDataMapper;
import com.mhmd.notion_fuse.notion_databases.model.MergeRequestDto;
import com.mhmd.notion_fuse.notion_databases.repository.SyncedDatabaseRepository;
import com.mhmd.notion_fuse.notion_databases.services.NotionDatabaseService;
import com.mhmd.notion_fuse.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotionDatabaseServiceTest {

    @Mock
    private SyncedDatabaseRepository syncedDatabaseRepository;

    @Mock
    private NotionClient notionClient;

    @Mock
    private IntegrationRepository integrationRepository;

    @Mock // 🌟 Mock the mapper causing the new NullPointerException
    private NotionDataMapper notionDataMapper;

    @InjectMocks
    private NotionDatabaseService notionDatabaseService;

    @Test
    void testInitiateDatabaseMerge_ShouldExecuteSuccessfully() {
        // 1. ARRANGE
        User mockUser = new User();
        mockUser.setId(1L);

        MergeRequestDto requestDto = new MergeRequestDto(
                "source-db-id-A",
                "source-db-id-B",
                "My Merged Production Board"
        );

        // Stub the integration repo call
        when(integrationRepository.findByUserIdAndPlatform(eq(1L), any(Platform.class)))
                .thenReturn(Optional.of(new com.mhmd.notion_fuse.integration.model.Integration()));

        // Stub the client response
        String fakeNotionJsonResponse = "{\"id\": \"generated-destination-uuid-1234\"}";
        when(notionClient.createMergedDatabase(any(), any(), any()))
                .thenReturn(fakeNotionJsonResponse);

        // 🌟 Stub the mapper to parse that fake JSON string and return a clean ID
        when(notionDataMapper.extractDatabaseId(eq(fakeNotionJsonResponse)))
                .thenReturn("generated-destination-uuid-1234");

        // 2. ACT & 3. ASSERT
        assertDoesNotThrow(() -> {
            notionDatabaseService.initiateDatabaseMerge(1L, requestDto, mockUser);
        });

        verify(syncedDatabaseRepository, times(1)).save(any());
        System.out.println("✅ Real code execution path completely verified!");
    }
}
