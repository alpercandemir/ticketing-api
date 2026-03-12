package com.example.ticketing.repository;

import com.example.ticketing.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOwnerId(Long ownerId);

    @Query("""
        SELECT e FROM Event e WHERE e.published = true
        AND (:from IS NULL OR e.startsAt >= :from)
        AND (:to IS NULL OR e.startsAt <= :to)
        AND (:q IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(e.venue) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY e.startsAt ASC
    """)
    List<Event> discoverEvents(@Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to,
                               @Param("q") String q);
}
