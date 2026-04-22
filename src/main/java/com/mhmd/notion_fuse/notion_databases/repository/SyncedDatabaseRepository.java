package com.mhmd.notion_fuse.notion_databases.repository;

import com.mhmd.notion_fuse.notion_databases.entity.SyncedDatabase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyncedDatabaseRepository extends JpaRepository<SyncedDatabase, Long> {
    // Finds all active merged configuration mappings belonging to a specific user
    List<SyncedDatabase> findByUserId(Long userId);
}
