

package com.campus.ems.service;

import com.campus.ems.model.*;
import com.campus.ems.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void createNotification(User user, String title, String message,
                                    Notification.NotifType type, Long relatedId) {
        Notification n = Notification.builder()
                .user(user).title(title).message(message)
                .type(type).relatedId(relatedId).isRead(false).build();
        notificationRepository.save(n);
    }
}
