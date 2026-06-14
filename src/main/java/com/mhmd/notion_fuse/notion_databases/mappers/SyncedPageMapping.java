package com.mhmd.notion_fuse.notion_databases.mappers;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "synced_page_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncedPageMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long syncConfigId;

    @Column(nullable = false)
    private String sourcePageId;

    @Column(nullable = false)
    private String destinationPageId;
}