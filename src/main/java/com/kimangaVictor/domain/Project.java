package com.kimangaVictor.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    private String title;

    /** Display number shown on the card, e.g. "01". Kept as text to preserve the leading zero. */
    private String number;

    /** Section heading the card sits under, e.g. "Personal Projects". */
    private String category;

    @Column(length = 2000)
    private String description;

    /** Human-readable date range, e.g. "May 2026 – Present". */
    private String period;

    private String repoUrl;
    private String liveUrl;
    private String thumbnailUrl;
    private boolean featured;
    private int sortOrder;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_tag", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "tag")
    @OrderColumn(name = "tag_order")
    private List<String> tags = new ArrayList<>();
}
