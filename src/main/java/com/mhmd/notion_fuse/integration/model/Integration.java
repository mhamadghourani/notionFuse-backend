package com.mhmd.notion_fuse.integration.model;

import com.mhmd.notion_fuse.integration.constant.Platform;
import com.mhmd.notion_fuse.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
public class Integration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @Enumerated(EnumType.STRING)
    private Platform platform;
    @Column(length = 2000)
    private String accessToken;

    private String refreshToken;
    private LocalDateTime expiresAt;

    private String workSpaceId;
    private String workSpaceName;
    private String botId;

    private LocalDateTime createdAt = LocalDateTime.now();
}
