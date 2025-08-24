package org.example.studylog.service.oauth;

import lombok.RequiredArgsConstructor;
import org.example.studylog.client.KakaoApiClient;
import org.example.studylog.client.KakaoAuthClient;
import org.example.studylog.dto.oauth.OAuthTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final KakaoAuthClient kakaoAuthClient; // https://kauth.kakao.com
    private final KakaoApiClient kakaoApiClient;   // https://kapi.kakao.com

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret:}")
    private String clientSecret;

    // Admin Key는 서버 비밀(.env/Parameter Store/KMS 등)로 관리
    @Value("${kakao.admin-key:}")
    private String adminKey;

    public boolean hasAdminKey() {
        return adminKey != null && !adminKey.isBlank();
    }

    // refresh -> access 재발급
    public OAuthTokenResponse refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }
        form.add("refresh_token", refreshToken);
        return kakaoAuthClient.refreshToken(form);
    }

    // (사용자 토큰 방식) access_token으로 언링크
    public void unlinkWithAccessToken(String accessToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        kakaoApiClient.unlink("Bearer " + accessToken, form);
    }

    // (서버 Admin Key 방식) kakao user_id로 언링크
    public void unlinkWithAdminKey(long kakaoUserId){
        if (adminKey == null || adminKey.isBlank()){
            throw new IllegalStateException("Kakao Admin Key is not configured.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("target_id_type", "user_id");
        form.add("target_id", String.valueOf(kakaoUserId));

        kakaoApiClient.unlink("KakaoAK " + adminKey, form);

    }
}
