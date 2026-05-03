package com.campus.ems.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Announcement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false) private User author;
    @Enumerated(EnumType.STRING) @Column(name = "target_role")
    private TargetRole targetRole = TargetRole.ALL;
    @Column(name = "is_pinned") private boolean isPinned = false;
    @Column(name = "expires_at") private LocalDateTime expiresAt;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
    public enum TargetRole { ALL, STUDENT, FACULTY, STAFF }
}
