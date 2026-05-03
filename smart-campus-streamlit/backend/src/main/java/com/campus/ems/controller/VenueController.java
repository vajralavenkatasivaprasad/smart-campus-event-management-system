package com.campus.ems.controller;

import com.campus.ems.model.*;
import com.campus.ems.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
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
