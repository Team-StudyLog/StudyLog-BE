package org.example.studylog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.studylog.entity.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private NotificationType type;

    private String content;

    private LocalDateTime createdAt;
}
