package com.campus.ems.controller;

import com.campus.ems.model.*;
import com.campus.ems.repository.*;
import com.campus.ems.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository registrationRepository;
    private final VenueRepository venueRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationService notificationService;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<?> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Page<Event> events;
        if (search != null && !search.isEmpty()) {
            events = eventRepository.searchPublished(search, pageable);
        } else if (category != null && !category.isEmpty()) {
            events = eventRepository.findByStatusAndCategory(Event.Status.PUBLISHED,
                    Event.Category.valueOf(category.toUpperCase()), pageable);
        } else {
            events = eventRepository.findByStatus(Event.Status.PUBLISHED, pageable);
        }
        return ResponseEntity.ok(Map.of(
                "events", events.getContent(),
                "totalPages", events.getTotalPages(),
                "totalElements", events.getTotalElements(),
                "currentPage", page
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable Long id) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) return ResponseEntity.notFound().build();
        Double avgRating = feedbackRepository.avgRatingByEvent(id);
        List<Feedback> feedbacks = feedbackRepository.findByEventId(id);
        return ResponseEntity.ok(Map.of(
                "event", event,
                "avgRating", avgRating != null ? avgRating : 0,
                "feedbackCount", feedbacks.size()
        ));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STAFF')")
    public ResponseEntity<?> createEvent(@RequestBody Map<String, Object> req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizer = userRepository.findByEmail(email).orElseThrow();
        Event event = new Event();
        event.setTitle((String) req.get("title"));
        event.setDescription((String) req.get("description"));
        event.setCategory(Event.Category.valueOf((String) req.getOrDefault("category", "OTHER")));
        event.setStartDate(LocalDateTime.parse((String) req.get("startDate")));
        event.setEndDate(LocalDateTime.parse((String) req.get("endDate")));
        if (req.get("registrationDeadline") != null)
            event.setRegistrationDeadline(LocalDateTime.parse((String) req.get("registrationDeadline")));
        event.setMaxAttendees((Integer) req.get("maxAttendees"));
        event.setOrganizer(organizer);
        event.setStatus(Event.Status.PUBLISHED);
        event.setFree(Boolean.TRUE.equals(req.get("isFree")));
        event.setLocationName((String) req.get("locationName"));
        if (req.get("venueId") != null) {
            venueRepository.findById(Long.valueOf(req.get("venueId").toString()))
                    .ifPresent(event::setVenue);
        }
        eventRepository.save(event);
        return ResponseEntity.ok(Map.of("message", "Event created successfully", "event", event));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STAFF')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) return ResponseEntity.notFound().build();
        if (req.get("title") != null) event.setTitle((String) req.get("title"));
        if (req.get("description") != null) event.setDescription((String) req.get("description"));
        if (req.get("status") != null) event.setStatus(Event.Status.valueOf((String) req.get("status")));
        eventRepository.save(event);
        return ResponseEntity.ok(Map.of("message", "Event updated", "event", event));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        eventRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Event deleted"));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerForEvent(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) return ResponseEntity.notFound().build();
        if (registrationRepository.existsByEventIdAndUserId(id, user.getId()))
            return ResponseEntity.badRequest().body(Map.of("message", "Already registered for this event"));
        if (event.getMaxAttendees() != null && event.getCurrentAttendees() >= event.getMaxAttendees())
            return ResponseEntity.badRequest().body(Map.of("message", "Event is full"));
        if (event.getRegistrationDeadline() != null && LocalDateTime.now().isAfter(event.getRegistrationDeadline()))
            return ResponseEntity.badRequest().body(Map.of("message", "Registration deadline passed"));

        String ticketNum = "TKT-" + id + "-" + user.getId() + "-" + System.currentTimeMillis();
        String qrData = "EventID:" + id + ";UserID:" + user.getId() + ";Ticket:" + ticketNum;
        String qrBase64 = qrCodeService.generateQRCodeBase64(qrData);

        EventRegistration reg = EventRegistration.builder()
                .event(event).user(user)
                .ticketNumber(ticketNum).qrCode(qrBase64)
                .registrationStatus(EventRegistration.RegistrationStatus.CONFIRMED)
                .paymentStatus(EventRegistration.PaymentStatus.PAID)
                .build();
        registrationRepository.save(reg);

        event.setCurrentAttendees(event.getCurrentAttendees() + 1);
        eventRepository.save(event);
        notificationService.createNotification(user, "Event Registered",
                "You have successfully registered for " + event.getTitle(), Notification.NotifType.EVENT, id);
        emailService.sendRegistrationConfirmation(user.getEmail(), user.getName(), event.getTitle(), ticketNum);

        return ResponseEntity.ok(Map.of(
                "message", "Successfully registered",
                "ticketNumber", ticketNum,
                "qrCode", qrBase64
        ));
    }

    @DeleteMapping("/{id}/unregister")
    public ResponseEntity<?> unregisterFromEvent(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        EventRegistration reg = registrationRepository.findByEventIdAndUserId(id, user.getId()).orElse(null);
        if (reg == null) return ResponseEntity.badRequest().body(Map.of("message", "Not registered"));
        registrationRepository.delete(reg);
        Event event = eventRepository.findById(id).orElseThrow();
        event.setCurrentAttendees(Math.max(0, event.getCurrentAttendees() - 1));
        eventRepository.save(event);
        return ResponseEntity.ok(Map.of("message", "Unregistered successfully"));
    }

    @GetMapping("/{id}/registrations")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<?> getRegistrations(@PathVariable Long id) {
        return ResponseEntity.ok(registrationRepository.findByEventId(id));
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) return ResponseEntity.notFound().build();
        if (feedbackRepository.findByEventIdAndUserId(id, user.getId()).isPresent())
            return ResponseEntity.badRequest().body(Map.of("message", "Feedback already submitted"));
        Feedback fb = Feedback.builder()
                .event(event).user(user)
                .rating((Integer) req.get("rating"))
                .comment((String) req.get("comment"))
                .build();
        feedbackRepository.save(fb);
        return ResponseEntity.ok(Map.of("message", "Feedback submitted successfully"));
    }

    @GetMapping("/my-events")
    public ResponseEntity<?> getMyRegistrations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(registrationRepository.findByUserId(user.getId()));
    }

    @GetMapping("/my-organized")
    public ResponseEntity<?> getMyOrganizedEvents() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(user.getId()));
    }
}
