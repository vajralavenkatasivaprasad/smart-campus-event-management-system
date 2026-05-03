package com.campus.ems.controller;

import com.campus.ems.model.*;
import com.campus.ems.repository.*;
import com.campus.ems.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

// ===================== VENUE CONTROLLER =====================
@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
// BUG FIX: Changed from package-private class to public class so Spring can proxy it for @PreAuthorize
public class VenueController {
    private final VenueRepository venueRepository;

    @GetMapping
    public ResponseEntity<?> getAll() { return ResponseEntity.ok(venueRepository.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return venueRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Venue venue) {
        return ResponseEntity.ok(venueRepository.save(venue));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Venue updated) {
        return venueRepository.findById(id).map(v -> {
            v.setName(updated.getName());
            v.setDescription(updated.getDescription());
            v.setCapacity(updated.getCapacity());
            v.setLocation(updated.getLocation());
            v.setAvailable(updated.isAvailable());
            return ResponseEntity.ok(venueRepository.save(v));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        venueRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Venue deleted"));
    }
}

// ===================== ANNOUNCEMENT CONTROLLER =====================
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
// BUG FIX: Changed from package-private class to public class
public class AnnouncementController {
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String role) {
        List<Announcement.TargetRole> roles = new ArrayList<>();
        roles.add(Announcement.TargetRole.ALL);
        if (role != null) {
            try { roles.add(Announcement.TargetRole.valueOf(role.toUpperCase())); }
            catch (Exception ignored) {}
        }
        return ResponseEntity.ok(announcementRepository.findByTargetRoleInOrderByIsPinnedDescCreatedAtDesc(roles));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByEmail(email).orElseThrow();
        Announcement ann = Announcement.builder()
                .title((String) req.get("title"))
                .content((String) req.get("content"))
                .author(author)
                .targetRole(Announcement.TargetRole.valueOf(
                        req.getOrDefault("targetRole", "ALL").toString()))
                .isPinned(Boolean.TRUE.equals(req.get("isPinned")))
                .build();
        return ResponseEntity.ok(announcementRepository.save(ann));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        announcementRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}

// ===================== NOTIFICATION CONTROLLER =====================
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
// BUG FIX: Changed from package-private class to public class
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getMyNotifications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(Map.of("count", notificationRepository.countByUserIdAndIsReadFalse(user.getId())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }
}

// ===================== CHATBOT CONTROLLER =====================
@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
// BUG FIX: Changed from package-private class to public class
public class ChatbotController {
    private final EventRepository eventRepository;
    private final AnnouncementRepository announcementRepository;
    private final ChatMessageRepository chatMessageRepository;

    @PostMapping("/message")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> req) {
        String message = req.get("message").toLowerCase().trim();
        String response = generateResponse(message);
        ChatMessage cm = ChatMessage.builder()
                .sessionId(req.getOrDefault("sessionId", "anonymous"))
                .message(req.get("message"))
                .response(response)
                .build();
        chatMessageRepository.save(cm);
        return ResponseEntity.ok(Map.of("response", response, "timestamp", java.time.LocalDateTime.now()));
    }

    private String generateResponse(String msg) {
        if (msg.contains("event") || msg.contains("events")) {
            long count = eventRepository.countPublished();
            return "There are currently " + count + " upcoming events! Browse them on the Events page.";
        }
        if (msg.contains("register") || msg.contains("sign up")) {
            return "To register: 1) Go to Events, 2) Click the event, 3) Click 'Register Now'. You'll get a confirmation email!";
        }
        if (msg.contains("venue") || msg.contains("location") || msg.contains("where")) {
            return "Venue details are on each event's page. We also have an interactive map showing all campus venues!";
        }
        if (msg.contains("cancel") || msg.contains("unregister")) {
            return "To cancel a registration, go to 'My Events' in your dashboard and click 'Unregister'.";
        }
        if (msg.contains("ticket") || msg.contains("qr")) {
            return "After registering, your QR code ticket is in 'My Events'. Show it at the event entrance!";
        }
        if (msg.contains("announcement") || msg.contains("news") || msg.contains("update")) {
            long count = announcementRepository.count();
            return "There are " + count + " announcements. Check the Announcements tab for the latest campus news!";
        }
        if (msg.contains("contact") || msg.contains("help") || msg.contains("support")) {
            return "For support, email support@campus.edu or call +91-XXX-XXX-XXXX. Available Mon-Fri, 9 AM - 5 PM.";
        }
        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) {
            return "Hello! 👋 I'm the Smart Campus EMS Assistant. I can help with events, registrations, venues, and more!";
        }
        if (msg.contains("thank")) {
            return "You're welcome! 😊 Is there anything else I can help you with?";
        }
        if (msg.contains("category") || msg.contains("type")) {
            return "Event categories: Academic, Cultural, Sports, Workshop, Seminar, Conference, Social. Use filters on the Events page!";
        }
        return "I'm here to help with campus events! Ask me about:\n• Upcoming events\n• How to register\n• Venue information\n• Tickets & QR codes\n• Announcements";
    }
}

// ===================== ADMIN CONTROLLER =====================
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
// BUG FIX: Changed from package-private class to public class
public class AdminController {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final VenueRepository venueRepository;

    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(Map.of(
                "totalUsers", userRepository.count(),
                "totalEvents", eventRepository.count(),
                "publishedEvents", eventRepository.countPublished(),
                "completedEvents", eventRepository.countCompleted(),
                "totalRegistrations", registrationRepository.countTotalRegistrations(),
                "totalVenues", venueRepository.count()
        ));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() { return ResponseEntity.ok(userRepository.findAll()); }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @PutMapping("/events/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEventStatus(@PathVariable Long id, @RequestBody Map<String, String> req) {
        return eventRepository.findById(id).map(e -> {
            e.setStatus(Event.Status.valueOf(req.get("status")));
            return ResponseEntity.ok(eventRepository.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }
}
