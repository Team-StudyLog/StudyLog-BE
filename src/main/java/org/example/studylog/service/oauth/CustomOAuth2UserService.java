package org.example.studylog.service.oauth;

import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.*;
import org.example.studylog.entity.user.Role;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        System.out.println(registrationId);

        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("kakao")){
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());

        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        // 로그인 완료시 로직
        String oauthId = oAuth2Response.getProvider()+"_"+oAuth2Response.getProviderId();
        System.out.println("유저네임: " + oauthId);

        // oauthId으로 로그인한 유저가 이미 존재하는지 확인
        User existData = userRepository.findByOauthId(oauthId);
        System.out.println("현재 데이터: " + existData);

        if (existData == null) {
            User user = User.builder()
                    .nickname(oAuth2Response.getName())
                    .profileImage(oAuth2Response.getProfileImage())
                    .level(0)
                    .recordCount(0L)
                    .role(Role.ROLE_USER)
                    .isProfileCompleted(false)
                    .uuid(UUID.randomUUID())
                    .code(generateCode())
                    .oauthId(oauthId)
                    .build();

            userRepository.save(user);

            UserDTO userDTO = new UserDTO();
            userDTO.setOauthId(oauthId);
            userDTO.setNickname(oAuth2Response.getName());
            userDTO.setRole(String.valueOf(user.getRole()));
            userDTO.setProfileCompleted(user.isProfileCompleted());

            return new CustomOAuth2User(userDTO);
        }
        else {
            UserDTO userDTO = new UserDTO();
            userDTO.setOauthId(existData.getOauthId());
            userDTO.setNickname(existData.getNickname());
            userDTO.setRole(String.valueOf(existData.getRole()));
            userDTO.setProfileCompleted(existData.isProfileCompleted());

            return new CustomOAuth2User(userDTO);
        }
    }

    private String generateCode(){
        String code;
        do{
            code = createCode(5);
        } while(userRepository.existsByCode(code));
        return code;
    }

    private String createCode(int length){
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for(int i=0; i<length; i++){
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
