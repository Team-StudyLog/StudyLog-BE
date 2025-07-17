package org.example.studylog.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_CODE_NOT_FOUND(404,"코드에 해당하는 사용자가 없습니다."),
    ALREADY_FRIEND(400,"이미 친구입니다."),
    ;

    private int status;
    private final String message;

    ErrorCode(int status, String message){
        this.status = status;
        this.message = message;
    }
}
