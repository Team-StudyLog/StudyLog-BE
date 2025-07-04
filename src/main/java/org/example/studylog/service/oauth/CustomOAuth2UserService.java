package org.example.studylog.service.oauth;

import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.dto.oauth.KakaoResponse;
import org.example.studylog.dto.oauth.OAuth2Response;
import org.example.studylog.dto.oauth.UserDTO;
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

        } else {
            return null;
        }

        // 로그인 완료시 로직
        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        System.out.println("유저네임: " + username);

        // username으로 로그인한 유저가 이미 존재하는지 확인
        User existData = userRepository.findByUsername(username);
        System.out.println("현재 데이터: " + existData);

        if (existData == null) {
            User user = User.builder()
                    .nickname(oAuth2Response.getName())
                    .profileImage(oAuth2Response.getProfileImage())
                    .level(0)
                    .role(Role.ROLE_USER)
                    .isProfileCompleted(false)
                    .uuid(UUID.randomUUID())
                    .code(generateCode())
                    .username(username)
                    .build();

            userRepository.save(user);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(String.valueOf(user.getRole()));

            return new CustomOAuth2User(userDTO);
        }
        else {
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(existData.getUsername());
            userDTO.setName(existData.getNickname());
            userDTO.setRole(String.valueOf(existData.getRole()));

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
