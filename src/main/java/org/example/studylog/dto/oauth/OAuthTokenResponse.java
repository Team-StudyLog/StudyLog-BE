package org.example.studylog.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoTokenResponse {

    @JsonProperty("token_type")
    private String tokenType;           // "bearer"
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private Long expiresIn;
    @JsonProperty("scope")
    private String scope;               // 선택

    // refresh 요청 시, 남은 유효기간이 짧을 때만 새 refresh_token을 줄 수 있음
    @JsonProperty("refresh_token")
    private String refreshToken;        // 선택
    @JsonProperty("refresh_token_expires_in")
    private Long refreshTokenExpiresIn; // 선택
}
