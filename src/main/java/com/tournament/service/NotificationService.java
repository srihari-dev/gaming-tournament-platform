package com.tournament.service;

import com.tournament.model.Notification;
import com.tournament.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUser_UserIdOrderBySentTimeDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Integer userId) {
        return notificationRepository.findByUser_UserIdAndReadFalseOrderBySentTimeDesc(userId);
    }

    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUser_UserIdAndReadFalse(userId);
    }

    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Integer userId) {
        List<Notification> unread = notificationRepository
            .findByUser_UserIdAndReadFalseOrderBySentTimeDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
