package com.mhmd.notion_fuse.notion_databases.model;

public record MergeHistoryDto(
        String id,
        String databaseName,
        String status,
        String createdAt,
        String NotionUrl
) {}
