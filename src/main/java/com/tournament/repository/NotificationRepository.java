package com.tournament.repository;

import com.tournament.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUser_UserIdOrderBySentTimeDesc(Integer userId);
    List<Notification> findByUser_UserIdAndReadFalseOrderBySentTimeDesc(Integer userId);
    long countByUser_UserIdAndReadFalse(Integer userId);
}
