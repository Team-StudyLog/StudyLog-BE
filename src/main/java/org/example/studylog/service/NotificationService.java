package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import org.example.studylog.dto.notification.NotificationDTO;
import org.example.studylog.dto.notification.NotificationListResponseDTO;
import org.example.studylog.dto.notification.NotificationResponseDTO;
import org.example.studylog.entity.notification.Notification;
import org.example.studylog.entity.user.User;
import org.example.studylog.exception.BusinessException;
import org.example.studylog.exception.ErrorCode;
import org.example.studylog.repository.EmitterRepository;
import org.example.studylog.repository.NotificationRepository;
import org.example.studylog.repository.UserRepository;
import org.example.studylog.util.TimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NotificationService {
    private final static Long DEFAULT_TIMEOUT = 60 * 60 * 1000L;
    private final static String NOTIFICATION_NAME = "notification";

    private final EmitterRepository emitterRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public SseEmitter createEmitter(String oauthId) {
        // (구독 요청이 들어오면) 새로운 SseEmitter 객체를 만든다.
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // oauthId로 SseEmitter를 저장한다.
        emitterRepository.save(oauthId, emitter);

        emitter.onCompletion(() -> emitterRepository.delete(oauthId));
        emitter.onTimeout(() -> emitterRepository.delete(oauthId));
        emitter.onError((e) -> emitterRepository.delete(oauthId));

        // 첫 연결 시 응답 더미 데이터 (503 에러 방지)
        try {
            emitter.send(SseEmitter.event().id("").name(NOTIFICATION_NAME).data("Connection completed"));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.NOTIFICATION_CONNECTION_ERROR);
        }

        return emitter;
    }

    // 클라이언트로 알림 보내기
    @Transactional
    public void sendToClient(String oauthId, NotificationDTO dto) {
        // 알림을 DB에 저장하기
        User user = userRepository.findByOauthId(oauthId);
        Notification notification = Notification.builder()
                .user(user)
                .type(dto.getType())
                .content(dto.getContent())
                .build();
        notificationRepository.save(notification);

        // 알림 보낼 대상의 SSE 객체가 있다면 알림 전송
        SseEmitter emitter = emitterRepository.get(oauthId);
        if(emitter != null){
            try {
                NotificationResponseDTO resDTO = NotificationResponseDTO.builder()
                                .type(dto.getType().getLabel())
                                .content(dto.getContent()).build();
                emitter.send(SseEmitter.event()
                        .name(NOTIFICATION_NAME)
                        .data(resDTO));
            } catch (IOException e){
                // IOException 발생하면 저장된 emitter를 삭제
                emitter.completeWithError(e);
                emitterRepository.delete(oauthId);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationListResponseDTO> getNotificationList(String oauthId) {
        User user = userRepository.findByOauthId(oauthId);
        List<Notification> notifications = notificationRepository.findTop30ByUserOrderByCreatedAtDesc(user);

        List<NotificationListResponseDTO> list = notifications.stream()
                .map(NotificationListResponseDTO::from)
                .collect(Collectors.toList());

        return list;
    }
}
