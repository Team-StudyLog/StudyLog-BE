package org.example.studylog.dto.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthTokenResponse {
    @JsonProperty("access_token") private String accessToken;
    @JsonProperty("expires_in")   private Long   expiresIn;
    @JsonProperty("token_type")   private String tokenType; // "Bearer"/"bearer"
    @JsonProperty("scope")        private String scope;     // 선택
    @JsonProperty("refresh_token") private String refreshToken; // 선택
    // 카카오 전용(선택)
    @JsonProperty("refresh_token_expires_in") private Long refreshTokenExpiresIn;
    // 구글 OIDC 선택
    @JsonProperty("id_token") private String idToken;
}