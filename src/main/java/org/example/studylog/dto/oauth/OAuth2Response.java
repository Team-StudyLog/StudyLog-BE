package org.example.studylog.dto.oauth;

public interface OAuth2Response {

    // 제공자 (Ex. kakao, google...)
    String getProvider();

    // 제공자에서 발급해주는 아이디(번호)
    String getProviderId();

    // 사용자 이름
    String getName();

    // 사용자 프로필이미지
    String getProfileImage();

}
