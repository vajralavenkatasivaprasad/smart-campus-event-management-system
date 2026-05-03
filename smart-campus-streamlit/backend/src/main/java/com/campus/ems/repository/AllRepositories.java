package com.campus.ems.repository;

import com.campus.ems.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

// BUG FIX: All repository interfaces changed from package-private to public.
// Spring Data JPA requires public interfaces to generate proxies and inject them.
// Package-private interfaces cause NoSuchBeanDefinitionException at runtime.

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

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

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    // BUG FIX: isAvailable is a boolean field — Spring Data derives findByIsAvailableTrue correctly
    List<Venue> findByIsAvailableTrue();
}

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);
    List<EventRegistration> findByUserId(Long userId);
    List<EventRegistration> findByEventId(Long eventId);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    long countByEventId(Long eventId);
    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.registrationStatus = 'CONFIRMED'")
    long countTotalRegistrations();
}

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByTargetRoleInOrderByIsPinnedDescCreatedAtDesc(List<Announcement.TargetRole> roles);
}

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByEventId(Long eventId);
    Optional<Feedback> findByEventIdAndUserId(Long eventId, Long userId);
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event.id = :eventId")
    Double avgRatingByEvent(@Param("eventId") Long eventId);
}

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
}
