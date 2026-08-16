package com.kimangaVictor.repository;

import com.kimangaVictor.domain.SkillGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillGroupRepository extends JpaRepository<SkillGroup, Long> {
    List<SkillGroup> findAllByOrderBySortOrderAsc();
}
