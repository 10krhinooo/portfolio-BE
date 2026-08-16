package com.kimangaVictor.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skill_group")
@Getter
@Setter
@NoArgsConstructor
public class SkillGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    private String title;

    /** Icon key the frontend maps to an inline SVG. */
    private String icon;

    private int sortOrder;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "skill_item", joinColumns = @JoinColumn(name = "skill_group_id"))
    @Column(name = "item")
    @OrderColumn(name = "item_order")
    private List<String> items = new ArrayList<>();
}
