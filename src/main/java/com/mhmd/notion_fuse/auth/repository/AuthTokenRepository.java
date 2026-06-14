package com.mhmd.notion_fuse.auth.repository;

import com.mhmd.notion_fuse.auth.entity.AuthToken;
import com.mhmd.notion_fuse.auth.entity.AuthTokenType;
import com.mhmd.notion_fuse.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTokenAndType(String token, AuthTokenType type);

    List<AuthToken> findByUserAndTypeAndUsedAtIsNull(User user, AuthTokenType type);
}
