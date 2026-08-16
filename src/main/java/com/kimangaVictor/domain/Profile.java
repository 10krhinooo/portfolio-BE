package com.kimangaVictor.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The single site-owner record. Exactly one row is ever seeded. */
@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String title;
    private String location;

    @Column(length = 1000)
    private String tagline;

    private String email;
    private String phone;
    private String githubUrl;
    private String linkedinUrl;
    private String resumeUrl;
}
