package com.mhmd.notion_fuse.notion_databases.repository;

import com.mhmd.notion_fuse.notion_databases.entity.MergeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MergeHistoryRepository extends JpaRepository<MergeHistoryEntity, Long> {
    // Custom finder to keep the dashboard performant
    List<MergeHistoryEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
