package org.example.studylog.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_CODE_NOT_FOUND(404,"코드에 해당하는 사용자가 없습니다."),
    ALREADY_FRIEND(400,"이미 친구입니다."),
    NOT_FRIEND(400, "친구 관계가 아닙니다."),
    FRIEND_NOT_FOUND(404, "해당하는 친구가 없습니다."),
    SELF_LOOKUP_NOT_ALLOWED(400, "자기 자신은 조회할 수 없습니다."),
    NOTIFICATION_CONNECTION_ERROR(500, "알림 서버와 연결이 실패하였습니다."),
    ;

    private int status;
    private final String message;

    ErrorCode(int status, String message){
        this.status = status;
        this.message = message;
    }
}
