package org.example.studylog.controller;

import lombok.RequiredArgsConstructor;
import org.example.studylog.dto.notification.NotificationListResponseDTO;
import org.example.studylog.service.NotificationService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        return notificationService.createEmitter(oauthId);
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotificationList() {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        List<NotificationListResponseDTO> list = notificationService.getNotificationList(oauthId);
        return ResponseUtil.buildResponse(200, "알림 목록 조회 완료", list);
    }

}
