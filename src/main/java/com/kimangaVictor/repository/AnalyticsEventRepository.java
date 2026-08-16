package com.kimangaVictor.repository;

import com.kimangaVictor.domain.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    long countByCreatedAtAfter(Instant since);

    @Query("select count(distinct e.visitorHash) from AnalyticsEvent e where e.createdAt > ?1")
    long countDistinctVisitorsSince(Instant since);

    @Query("select e.type, count(e) from AnalyticsEvent e group by e.type order by count(e) desc")
    List<Object[]> countByType();

    @Query("""
            select e.target, count(e) from AnalyticsEvent e
            where e.type = ?1 and e.target is not null
            group by e.target order by count(e) desc
            """)
    List<Object[]> countByTargetForType(String type);
}
