package org.example.studylog.event.listener;

import lombok.RequiredArgsConstructor;
import org.example.studylog.event.RecordEvent;
import org.example.studylog.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LevelUpListener {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void handleRecordEvent(RecordEvent event) {
        

        if(event.getAction() == RecordEvent.ActionType.CREATED){
            // 레벨 체크 (기록 생성 시)

        }
        else if(event.getAction() == RecordEvent.ActionType.DELETED){
            // 레벨 체크 (기록 삭제 시)
        }
    }
}
