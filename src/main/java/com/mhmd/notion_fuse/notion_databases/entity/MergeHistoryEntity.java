package com.mhmd.notion_fuse.notion_databases.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "merge_history")
@Data
public class MergeHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // Critical: Links data to the user

    private String databaseName;
    private String status; // SUCCESS, PENDING, FAILED
    private LocalDateTime createdAt;
    private String notionUrl;
}
