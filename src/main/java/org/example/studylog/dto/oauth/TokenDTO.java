package org.example.studylog.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenDTO {

    private String refreshToken;
    private String accessToken;
    private String code;
    private boolean isNewUser;

    @Getter
    @Builder
    public static class ResponseDTO {
        private String accessToken;
        private String code;
        private boolean isNewUser;
    }
}