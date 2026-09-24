package com.example.kite_images.service;

import com.example.kite_images.model.Notification;
import com.example.kite_images.model.User;
import com.example.kite_images.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification create(User user, User fromUser, String type, String text, Long pinId) {
        if (user.getId().equals(fromUser.getId())) {
            return null;
        }
        Notification n = new Notification();
        n.setUser(user);
        n.setFromUser(fromUser);
        n.setType(type);
        n.setText(text);
        n.setPinId(pinId);
        return notificationRepository.save(n);
    }

    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public int markMessageNotificationsAsRead(Long userId, Long fromUserId) {
        return notificationRepository.markMessageNotificationsAsRead(userId, fromUserId);
    }

    @Transactional
    public int markAllMessageNotificationsAsRead(Long userId) {
        return notificationRepository.markAllMessageNotificationsAsRead(userId);
    }

    @Transactional
    public int markPinNotificationsAsRead(Long userId, Long pinId) {
        return notificationRepository.markPinNotificationsAsRead(userId, pinId);
    }
}
