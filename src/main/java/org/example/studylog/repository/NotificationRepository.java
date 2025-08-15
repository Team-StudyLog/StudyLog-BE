package org.example.studylog.repository;

import org.example.studylog.entity.notification.Notification;
import org.example.studylog.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop30ByUserOrderByCreatedAtDesc(User user);

}
