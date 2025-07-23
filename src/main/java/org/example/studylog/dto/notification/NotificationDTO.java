package org.example.studylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.studylog.entity.notification.NotificationType;


@Builder
@AllArgsConstructor
@Getter
public class NotificationDTO {
    private NotificationType type;
    private String content;
}
