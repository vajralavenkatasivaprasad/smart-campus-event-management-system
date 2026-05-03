

package com.campus.ems.controller;

import com.campus.ems.model.*;
import com.campus.ems.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
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
