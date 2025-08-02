package org.example.studylog.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.entity.user.LevelThresholds;
import org.example.studylog.entity.user.User;
import org.example.studylog.event.LevelEvent;
import org.example.studylog.event.RecordEvent;
import org.example.studylog.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class LevelChangeListener {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void handleRecordEvent(RecordEvent event) {
        User currentUser = event.getUser();
        long recordCount = currentUser.getRecordCount();

        log.info("레벨 체크 시작: USER = {}, CURRENT_LEVEL = {}, RECORD_COUNT= {}",
                currentUser.getOauthId(), currentUser.getLevel(), recordCount);

        int lastLevel = currentUser.getLevel();
        int newLevel = LevelThresholds.getLevelForRecordCount(recordCount);
        if(lastLevel != newLevel){
            currentUser.setLevel(newLevel); // 레벨 업데이트

            if(lastLevel > newLevel){
                eventPublisher.publishEvent(new LevelEvent(currentUser, newLevel, LevelEvent.ActionType.DOWN));
            }
            else if(lastLevel < newLevel){
                eventPublisher.publishEvent(new LevelEvent(currentUser, newLevel, LevelEvent.ActionType.UP));
            }
        }
    }
}
