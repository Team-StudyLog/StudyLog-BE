package org.example.studylog.service.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.OAuthTokenResponse;
import org.example.studylog.entity.user.User;
import org.example.studylog.util.AesGcmEncryptor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalOAuthUnlinkService {

    private final GoogleOAuthService googleOAuthService;
    private final KakaoOAuthService kakaoOAuthService;
    private final AesGcmEncryptor encryptor;

    public void unlink(User user){
        if (user == null || user.getOauthId() == null) return;

        String oauthId = user.getOauthId();
        String[] parts = oauthId.split("_", 2);
        if(parts.length < 2){
            log.warn("올바른 oauthId 형식이 아님: {}", oauthId);
            return;
        }
        String provider = parts[0];
        String providerUserId = parts[1];

        try {
            if(provider.equals("google")){
                unlinkGoogle(user);
            }
            else if(provider.equals("kakao")){
                unlinkKakao(user, providerUserId);
            }
            else {
                log.info("지원하지 않는 provider: {}", provider);
            }
        } catch (Exception e){
            log.error("외부 연동 해제 실패: provider={}, oauthId={}, msg={}",
                    provider, oauthId, e.getMessage());
        }
    }

    private void unlinkGoogle(User user){
        String enc = user.getRefreshToken();
        String refresh = encryptor.decrypt(enc);  // 복호화

        // RefreshToken으로 revoke 요청 보내기
        if (refresh != null && !refresh.isBlank()){
            googleOAuthService.revoke(refresh);
            log.info("Google revoke by refresh_token 완료");
        } else {
            log.info("Google refresh_token 없음 -> revoke 생략");
        }
    }

    private void unlinkKakao(User user, String kakaoUserId){
        // 1순위: Admin Key 방식(사용자 토큰 불필요)
        if(kakaoOAuthService.hasAdminKey()){
            kakaoOAuthService.unlinkWithAdminKey(Long.parseLong(kakaoUserId));
            log.info("Kakao unlink by AdminKey 완료: user_id={}", kakaoUserId);
            return;
        }


        // 2순위: 사용자의 AccessToken으로 revoke 요청 보내기
        String enc = user.getRefreshToken();
        String refresh = encryptor.decrypt(enc);  // 복호화
        if (refresh != null && !refresh.isBlank()) {
            OAuthTokenResponse tr = kakaoOAuthService.refreshAccessToken(refresh);
            kakaoOAuthService.unlinkWithAccessToken(tr.getAccessToken());
            log.info("Kakao unlink by refreshed access_token 완료");
        } else {
            log.info("Kakao refresh_token 없음 -> revoke 생략");
        }
    }
}
