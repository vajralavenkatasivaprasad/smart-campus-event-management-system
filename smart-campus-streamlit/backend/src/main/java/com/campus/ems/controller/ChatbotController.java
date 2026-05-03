

package com.campus.ems.controller;

import com.campus.ems.model.*;
import com.campus.ems.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
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
