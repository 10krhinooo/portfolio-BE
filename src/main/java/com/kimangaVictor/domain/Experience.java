package com.kimangaVictor.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** One entry on the journey timeline — either a job or a qualification. */
@Entity
@Table(name = "experience")
@Getter
@Setter
@NoArgsConstructor
public class Experience {

    public enum Kind {WORK, EDUCATION}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    /** Job title, or the qualification for {@link Kind#EDUCATION} entries. */
    private String role;

    private String organisation;
    private String location;
    private String period;

    @Enumerated(EnumType.STRING)
    private Kind kind;

    private int sortOrder;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "experience_bullet", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "bullet", length = 1000)
    @OrderColumn(name = "bullet_order")
    private List<String> bullets = new ArrayList<>();
}
