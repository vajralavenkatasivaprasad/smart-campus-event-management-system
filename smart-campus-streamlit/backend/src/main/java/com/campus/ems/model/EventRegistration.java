package com.campus.ems.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status")
    private RegistrationStatus registrationStatus = RegistrationStatus.CONFIRMED;

    @Column(name = "ticket_number", unique = true)
    private String ticketNumber;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PAID;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "attended_at")
    private LocalDateTime attendedAt;

    @PrePersist
    protected void onCreate() { registeredAt = LocalDateTime.now(); }

    public enum RegistrationStatus { PENDING, CONFIRMED, CANCELLED, ATTENDED }
    public enum PaymentStatus { PENDING, PAID, REFUNDED }
}
