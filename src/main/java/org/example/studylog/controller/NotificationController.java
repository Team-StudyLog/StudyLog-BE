package org.example.studylog.controller;

import lombok.RequiredArgsConstructor;
import org.example.studylog.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        return notificationService.createEmitter(oauthId);
    }




}
