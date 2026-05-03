

package com.campus.ems.repository;

import com.campus.ems.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatus(Event.Status status, Pageable pageable);
    Page<Event> findByStatusAndCategory(Event.Status status, Event.Category category, Pageable pageable);
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Event> searchPublished(@Param("q") String query, Pageable pageable);
    List<Event> findByOrganizerIdOrderByCreatedAtDesc(Long organizerId);
    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = 'PUBLISHED'") long countPublished();
    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = 'COMPLETED'") long countCompleted();
}
