package org.example.studylog.dto.oauth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@Schema(name = "TokenDTO")
public class TokenDTO {

    private String refreshToken;
    private String accessToken;
    private String code;
    private boolean isNewUser;

    @Getter
    @Builder
    @Schema(name = "TokenResponseDTO")
    public static class ResponseDTO {
        private String accessToken;
        private String code;
        private boolean isNewUser;
    }
}