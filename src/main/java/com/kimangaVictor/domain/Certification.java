package com.kimangaVictor.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "certification")
@Getter
@Setter
@NoArgsConstructor
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    private String title;
    private String issuer;

    /** Award date as displayed, e.g. "March 2025". */
    private String awarded;

    private String credentialUrl;
    private int sortOrder;
}
