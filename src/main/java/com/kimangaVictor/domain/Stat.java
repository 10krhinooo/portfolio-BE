package com.kimangaVictor.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A headline figure in the hero stats row, e.g. "9+ / Projects Shipped". */
@Entity
@Table(name = "stat")
@Getter
@Setter
@NoArgsConstructor
public class Stat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** "value" is a reserved word in H2 and Postgres, so the column carries a prefix. */
    @Column(name = "stat_value")
    private String value;

    private String label;
    private int sortOrder;
}
