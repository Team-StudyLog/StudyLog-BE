package org.example.studylog.dto.oauth;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;
    private final Map<String, Object> kakaoAccount;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute;
        this.kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getName() {
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        return profile.get("nickname").toString();
    }

    @Override
    public String getProfileImage() {
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        return profile.get("profile_image_url").toString();
    }
}
