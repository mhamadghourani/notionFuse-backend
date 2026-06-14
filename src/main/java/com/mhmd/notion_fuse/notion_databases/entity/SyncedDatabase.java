package com.mhmd.notion_fuse.notion_databases.entity;

import com.mhmd.notion_fuse.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "synced_databases")
@Getter
@Setter
@NoArgsConstructor
public class SyncedDatabase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The Notion ID of the first source database to merge
    @Column(name = "source_database_a_id", nullable = false)
    private String sourceDatabaseAId;

    // The Notion ID of the second source database to merge
    @Column(name = "source_database_b_id", nullable = false)
    private String sourceDatabaseBId;

    // The Notion ID of the brand-new destination database created in their workspace
    @Column(name = "destination_database_id", nullable = false)
    private String destinationDatabaseId;

    @Column(name = "database_name")
    private String databaseName;

    // Automation setting toggle flag
    @Column(name = "is_automation_active", nullable = false)
    private boolean isAutomationActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
