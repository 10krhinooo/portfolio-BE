package com.kimangaVictor.repository;

import com.kimangaVictor.domain.Stat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatRepository extends JpaRepository<Stat, Long> {
    List<Stat> findAllByOrderBySortOrderAsc();
}
