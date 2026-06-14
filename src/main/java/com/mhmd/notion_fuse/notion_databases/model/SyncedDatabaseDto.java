package com.mhmd.notion_fuse.notion_databases.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncedDatabaseDto {
    private Long id;
    private String databaseName;
    private String status;
    private boolean isAutomationActive;
    private LocalDateTime createdAt;
}
