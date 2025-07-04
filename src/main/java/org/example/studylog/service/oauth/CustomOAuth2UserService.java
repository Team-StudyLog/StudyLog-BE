package org.example.studylog.service.oauth;

import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.dto.oauth.KakaoResponse;
import org.example.studylog.dto.oauth.OAuth2Response;
import org.example.studylog.dto.oauth.UserDTO;
import org.example.studylog.entity.Users;
import org.example.studylog.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

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

        } else {
            return null;
        }

        // 로그인 완료시 로직
        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        System.out.println("유저네임: " + username);

        // username으로 로그인한 유저가 이미 존재하는지 확인
        Users existData = userRepository.findByUsername(username);
        System.out.println("현재 데이터: " + existData);

        if (existData == null) {
            // 로깅
            log.info("username: {}", username);
            log.info("nickname: {}", oAuth2Response.getName());

            Users user = new Users();
            user.setUsername(username);
            user.setNickname(oAuth2Response.getName());
            user.setProfileImage(oAuth2Response.getProfileImage());
            user.setLevel(0);
            user.setUuid(UUID.randomUUID());
            user.setRole("ROLE_A");
//            user.setCode();

            try{
                userRepository.save(user);
            } catch (Exception e){
                log.error("User 저장 실패", e);
            }

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole("ROLE_A");

            return new CustomOAuth2User(userDTO);
        }
        // 데이터가 이미 존재하면 업데이트
        else {
            // 로깅
            log.info("username: {}", username);
            log.info("nickname: {}", oAuth2Response.getName());

            existData.setNickname(oAuth2Response.getName());
            existData.setProfileImage(oAuth2Response.getProfileImage());
            userRepository.save(existData);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(existData.getUsername());
            userDTO.setName(existData.getNickname());
            userDTO.setRole(existData.getRole());

            return new CustomOAuth2User(userDTO);
        }

    }
}
