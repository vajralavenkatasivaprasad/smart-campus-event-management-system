

package com.campus.ems.controller;

import com.campus.ems.model.*;
import com.campus.ems.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
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
