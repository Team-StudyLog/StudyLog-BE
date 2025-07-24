package org.example.studylog.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.studylog.entity.notification.Notification;
import org.example.studylog.util.TimeUtil;

@Builder
@AllArgsConstructor
@Getter
public class NotificationListResponseDTO {
    private String type;
    private String content;
    private String timeAgo;

    public static NotificationListResponseDTO from(Notification notification) {
        return NotificationListResponseDTO.builder()
                .type(notification.getType().getLabel())
                .content(notification.getContent())
                .timeAgo(TimeUtil.formatTimeAgo(notification.getCreatedAt()))
                .build();
    }
}
