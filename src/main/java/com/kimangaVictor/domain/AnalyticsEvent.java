package com.kimangaVictor.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A single visitor interaction.
 *
 * <p>No raw IP and no cookie is ever stored. {@code visitorHash} is a salted SHA-256 of the client
 * IP truncated to 16 hex chars — enough to count distinct visitors, not enough to identify one.
 */
@Entity
@Table(name = "analytics_event", indexes = {
        @Index(name = "idx_event_type", columnList = "type"),
        @Index(name = "idx_event_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String path;
    private String referrer;
    private String target;

    @Column(length = 16)
    private String visitorHash;

    private Instant createdAt;
}
