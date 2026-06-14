package com.mhmd.notion_fuse.integration.repository;

import com.mhmd.notion_fuse.integration.constant.Platform;
import com.mhmd.notion_fuse.integration.model.Integration;
import com.mhmd.notion_fuse.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationRepository extends JpaRepository<Integration, Long> {

    Optional<Integration> findByUserIdAndPlatform(Long userId, Platform platform);

}