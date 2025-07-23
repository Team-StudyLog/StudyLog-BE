package org.example.studylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class NotificationResponseDTO {
    private String type;
    private String content;
}
