package com.example.kite_images.repository;

import com.example.kite_images.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    int countByUserIdAndReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.fromUser.id = :fromUserId AND n.type = 'message' AND n.read = false")
    int markMessageNotificationsAsRead(@Param("userId") Long userId, @Param("fromUserId") Long fromUserId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.type = 'message' AND n.read = false")
    int markAllMessageNotificationsAsRead(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.pinId = :pinId AND n.type IN ('like','comment') AND n.read = false")
    int markPinNotificationsAsRead(@Param("userId") Long userId, @Param("pinId") Long pinId);
}
