package com.mhmd.notion_fuse.notion_databases.model;

public record MergeRequestDto(
        String sourceDatabaseAId,
        String sourceDatabaseBId,
        String customMergedDatabaseName,
        String targetParentPageId
) {}
