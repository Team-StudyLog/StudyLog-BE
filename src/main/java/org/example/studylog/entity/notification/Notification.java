package org.example.studylog.entity.notification;

import jakarta.persistence.*;
import lombok.*;
import org.example.studylog.entity.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isRead = false;
}
