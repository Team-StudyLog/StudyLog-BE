package org.example.studylog.entity.notification;

import lombok.Getter;

@Getter
public enum NotificationType {
    ADD_FRIEND("친구 추가"),
    DELETE_FRIEND("친구 삭제"),
    STREAK("스트릭"),
    BADGE("뱃지");

    private final String label;

    NotificationType(String label){
        this.label = label;
    }
}
