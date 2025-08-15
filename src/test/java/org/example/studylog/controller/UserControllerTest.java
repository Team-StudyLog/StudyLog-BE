package org.example.studylog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.example.studylog.dto.ProfileCreateRequestDTO;
import org.example.studylog.dto.ProfileResponseDTO;
import org.example.studylog.entity.user.User;
import org.example.studylog.jwt.JWTUtil;
import org.example.studylog.repository.UserRepository;
import org.example.studylog.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JWTUtil jwtUtil;

    @Test
    void 프로필_업데이트_성공() throws Exception {
        // given
        // ProfileCheckFilter에서 참조할 User mock 객체 설정
        User user = new User();
        user.setNickname("사용자");
        user.setIntro("한줄 소개입니다.");
        user.setProfileImage("https://example.com/test.png");
        user.setProfileCompleted(true);

        // 해당 user가 반환되도록 userRepository mocking
        when(userRepository.findByOauthId("abc1234")).thenReturn(user);

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage", "test.png", "image/png", "fake-image".getBytes());
        String nickname = "사용자11";
        String intro = "한줄 소개입니다.11";

        // 사용자 프로필 업데이트시, 컨트롤러가 반환하도록 기대하는 응답 값
        ProfileResponseDTO dto = ProfileResponseDTO.builder()
                .nickname(nickname)
                .intro(intro)
                .profileImage("https://example.com/test.png")
                .build();

        when(userService.createUserProfile(any(ProfileCreateRequestDTO.class), eq("abc1234")))
                .thenReturn(dto);

        // access 토큰을 쿠키에 담아 요청
        String token = jwtUtil.createJwt("access", "abc1234", "ROLE_USER", 60000L);
        mockMvc.perform(multipart("/users/profile")
                .file(profileImage)
                .param("nickname", nickname)
                .param("intro", intro)
                .with(request -> {request.setMethod("PUT"); return request; })
                                .cookie(new Cookie("access", token))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 프로필 업데이트 완료"))
                .andExpect(jsonPath("$.data.nickname").value("사용자11"))
                .andExpect(jsonPath("$.data.intro").value("한줄 소개입니다.11"));
    }

    @Test
    @WithMockUser(username = "abc1234")
    void 프로필_조회_성공() throws Exception {
        // given
        // ProfileCheckFilter에서 참조할 User mock 객체 설정
        User user = new User();
        user.setNickname("사용자");
        user.setIntro("한줄 소개입니다.");
        user.setProfileImage("https://example.com/test.png");
        user.setProfileCompleted(true);

        // 해당 user가 반환되도록 userRepository mocking
        when(userRepository.findByOauthId("abc1234")).thenReturn(user);

        ProfileResponseDTO dto = ProfileResponseDTO.builder()
                .nickname("사용자")
                .intro("안녕하세요")
                .profileImage("https://example.com/image.png")
                .build();

        when(userService.getUserProfile("abc1234")).thenReturn(dto);

        String token = jwtUtil.createJwt("access", "abc1234", "ROLE_USER", 60000L);
        mockMvc.perform(get("/users/profile")
                        .cookie(new Cookie("access", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("사용자 프로필 조회 성공"))
                .andExpect(jsonPath("$.data.nickname").value("사용자"));

    }
}