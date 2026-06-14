package com.mhmd.notion_fuse.notion_databases.repository;

import com.mhmd.notion_fuse.notion_databases.mappers.SyncedPageMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SyncedPageMappingRepository extends JpaRepository<SyncedPageMapping, Long> {
    Optional<SyncedPageMapping> findBySyncConfigIdAndSourcePageId(Long syncConfigId, String sourcePageId);
}
