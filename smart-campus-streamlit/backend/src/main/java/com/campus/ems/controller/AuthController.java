package com.campus.ems.controller;

import com.campus.ems.dto.RegisterRequest;
import com.campus.ems.model.User;
import com.campus.ems.repository.UserRepository;
import com.campus.ems.security.JwtUtils;
import com.campus.ems.service.EmailService;
import com.campus.ems.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }
        String otp = otpService.generateOtp();
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role(User.Role.valueOf(req.getRole() != null ? req.getRole() : "STUDENT"))
                .otp(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(10))
                .isVerified(false)
                .build();
        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), user.getName(), otp);
        return ResponseEntity.ok(Map.of("message", "Registration successful. Please verify your email with OTP."));
    }

    // BUG FIX: Removed invalid .orElseReturn() — not a valid Optional method.
    // Replaced with .orElse(null) + null check pattern.
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        if (user.isVerified()) return ResponseEntity.badRequest().body(Map.of("message", "Already verified"));
        if (user.getOtp() == null || !user.getOtp().equals(otp))
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP"));
        if (user.getOtpExpiry().isBefore(LocalDateTime.now()))
            return ResponseEntity.badRequest().body(Map.of("message", "OTP expired. Request a new one."));

        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        String token = jwtUtils.generateToken(user);
        return ResponseEntity.ok(Map.of(
                "message", "Email verified successfully",
                "token", token,
                "user", buildUserResponse(user)
        ));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        String otp = otpService.generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        emailService.sendOtpEmail(email, user.getName(), otp);
        return ResponseEntity.ok(Map.of("message", "OTP resent successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword()))
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        if (!user.isVerified())
            return ResponseEntity.status(403).body(Map.of(
                "message", "Please verify your email first",
                "needsVerification", true,
                "email", email));
        String token = jwtUtils.generateToken(user);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", buildUserResponse(user),
                "message", "Login successful"
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.ok(Map.of("message", "If email exists, OTP sent"));
        String otp = otpService.generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        emailService.sendPasswordResetEmail(email, user.getName(), otp);
        return ResponseEntity.ok(Map.of("message", "Password reset OTP sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");
        String newPassword = req.get("newPassword");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        if (!otp.equals(user.getOtp()) || user.getOtpExpiry().isBefore(LocalDateTime.now()))
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    private Map<String, Object> buildUserResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "isVerified", user.isVerified()
        );
    }
}
