package org.example.studylog.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.event.RecordCreatedEvent;
import org.example.studylog.event.RecordDeletedEvent;
import org.example.studylog.repository.custom.RankingRepositoryImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordEventListener {

    private final RankingRepositoryImpl rankingRepository;

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleRecordCreated(RecordCreatedEvent event) {
        log.info("랭킹 집계 증가 이벤트 발행: USER={}, YEAR={}, MONTH={}", event.getUserId(), event.getYear(), event.getMonth());
        rankingRepository.incrementOrInsert(event.getUserId(), event.getYear(), event.getMonth());
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleRecordDeleted(RecordDeletedEvent event){
        log.info("랭킹 집계 감소 이벤트 발행: USER={}, YEAR={}, MONTH={}", event.getUserId(), event.getYear(), event.getMonth());
        rankingRepository.decrement(event.getUserId(), event.getYear(), event.getMonth());
    }
}
