package org.example.studylog.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.notification.NotificationDTO;
import org.example.studylog.entity.notification.NotificationType;
import org.example.studylog.entity.user.User;
import org.example.studylog.event.LevelEvent;
import org.example.studylog.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    @Transactional
    public void handleLevelChange(LevelEvent event){
        User currentUser = event.getUser();
        log.info("레벨 변경 알림 전송: USER={}, LEVEL={}", currentUser.getOauthId(), event.getNewLevel());

        String message = "";
        if(event.getAction() == LevelEvent.ActionType.UP){
            message = String.format("Lv.%d 뱃지를 달성했습니다.", event.getNewLevel());
        }
        else if(event.getAction() == LevelEvent.ActionType.DOWN){
            message = String.format("Lv.%d 뱃지로 하락했습니다.", event.getNewLevel());
        }

        // 알림 보내기
        notificationService.sendToClient(
                currentUser.getOauthId(),
                NotificationDTO.builder()
                        .content(message)
                        .type(NotificationType.BADGE)
                        .build());
    }
}
