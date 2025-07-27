package org.example.studylog.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class EmitterRepository {
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter save(String oauthId, SseEmitter sseEmitter){
        emitterMap.put(oauthId, sseEmitter);
        log.info("Saved SseEmitter for {}", sseEmitter);
        return sseEmitter;
    }

    public SseEmitter get(String oauthId){
        log.info("Got SseEmitter for {}", oauthId);
        return emitterMap.get(oauthId);
    }

    public void delete(String oauthId) {
        emitterMap.remove(oauthId);
        log.info("Deleted SseEmitter for {}", oauthId);
    }
}
