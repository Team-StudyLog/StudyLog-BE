package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import org.example.studylog.dto.ProfileCreateRequestDTO;
import org.example.studylog.dto.ProfileResponseDTO;
import org.example.studylog.dto.UserInfoResponseDTO;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.FriendRepository;
import org.example.studylog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;
    private final FriendRepository friendRepository;

    @Transactional
    public ProfileResponseDTO createUserProfile(ProfileCreateRequestDTO request, String oauthId){
        // 유저 찾기
        User user = userRepository.findByOauthId(oauthId);

        MultipartFile file = request.getProfileImage();
        // S3 업로드
        String imageUrl = awsS3Service.uploadFile(file, user);

        // User 엔티티에 프로필 정보 업데이트
        user.setNickname(request.getNickname());
        user.setIntro(request.getIntro());
        user.setProfileImage(imageUrl);

        // 프로필 정보 업데이트 상태 바꾸기
        if(!user.isProfileCompleted())
            user.setProfileCompleted(true);

        userRepository.save(user);

        // 수정된 데이터로 응답 객체 반환
        return ProfileResponseDTO.builder()
                .nickname(user.getNickname())
                .intro(user.getIntro())
                .profileImage(imageUrl)
                .build();
    }

    @Transactional(readOnly = true)
    public ProfileResponseDTO getUserProfile(String oauthId) {
        // 유저 찾기
        User user = userRepository.findByOauthId(oauthId);

        ProfileResponseDTO dto = ProfileResponseDTO.builder()
                .nickname(user.getNickname())
                .intro(user.getIntro())
                .profileImage(user.getProfileImage())
                .build();

        return dto;
    }

    @Transactional(readOnly = true)
    public UserInfoResponseDTO getUserInfo(String oauthId) {
        // 유저 찾기
        User user = userRepository.findByOauthId(oauthId);

        Long count = friendRepository.countByUser(user);
        UserInfoResponseDTO dto = UserInfoResponseDTO.builder()
                .profileImage(user.getProfileImage())
                .nickname(user.getNickname())
                .intro(user.getIntro())
                .friendCount(count)
                .code(user.getCode())
                .build();

        return dto;
    }
}
