package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.*;
import org.example.studylog.entity.user.User;
import org.example.studylog.exception.UserNotFoundException;
import org.example.studylog.repository.FriendRepository;
import org.example.studylog.repository.RefreshRepository;
import org.example.studylog.repository.UserRepository;
import org.example.studylog.service.oauth.ExternalOAuthUnlinkService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;
    private final FriendRepository friendRepository;
    private final RefreshRepository refreshRepository;
    private final ExternalOAuthUnlinkService externalOAuthUnlinkService;

    @Transactional
    public ProfileResponseDTO createUserProfile(ProfileCreateRequestDTO request, String oauthId){
        // 유저 찾기
        User user = userRepository.findByOauthId(oauthId);

        MultipartFile file = request.getProfileImage();
        // S3 업로드
        String imageUrl = awsS3Service.uploadProfileImage(file, user);

        // User 엔티티에 프로필 정보 업데이트
        user.setNickname(request.getNickname());
        user.setIntro(request.getIntro());
        user.setProfileImage(imageUrl);

        // 프로필 정보 업데이트 상태 바꾸기
        if(!user.isProfileCompleted())
            user.setProfileCompleted(true);

        userRepository.save(user);

        // 생성된 데이터로 응답 객체 반환
        return ProfileResponseDTO.builder()
                .nickname(user.getNickname())
                .intro(user.getIntro())
                .profileImage(imageUrl)
                .build();
    }

    @Transactional
    public ProfileResponseDTO updateUserProfile(ProfileUpdateRequestDTO request, String oauthId) {
        // 유저 찾기
        User user = userRepository.findByOauthId(oauthId);

        // 닉네임 수정
        if(request.getNickname() != null){
            user.setNickname(request.getNickname());
        }

        // 한줄 소개 수정
        if(request.getIntro() != null){
            user.setIntro(request.getIntro());
        }

        // 프로필 이미지 수정
        if(request.getProfileImage() != null){
            MultipartFile newImage = request.getProfileImage();
            // S3 업로드
            String imageUrl = awsS3Service.uploadProfileImage(newImage, user);
            user.setProfileImage(imageUrl);
        }

        // 수정된 데이터로 응답 객체 반환
        return ProfileResponseDTO.builder()
                .nickname(user.getNickname())
                .intro(user.getIntro())
                .profileImage(user.getProfileImage())
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

    @Transactional
    public BackgroundDTO.ResponseDTO updateBackground(String oauthId, BackgroundDTO.RequestDTO dto) {
        // 유저 찾기
        User user = userRepository.findByOauthId(oauthId);

        log.info("배경화면 수정 시작: 사용자={}", oauthId);

        MultipartFile file = dto.getCoverImage();
        if(file.isEmpty()){
            throw new IllegalStateException("빈 파일은 업로드할 수 없습니다.");
        }

        // 이미지 URL을 DB에 저장
        String backImageUrl = awsS3Service.uploadBackImage(file, user);
        user.setBackImage(backImageUrl);

        // 응답 생성
        BackgroundDTO.ResponseDTO responseDTO = new BackgroundDTO.ResponseDTO(backImageUrl);

        log.info("배경화면 수정 완료: 사용자={}, 배경화면 url = {}", oauthId, backImageUrl);

        return responseDTO;
    }

    @Transactional
    public void deleteAccount(String oauthId) {
        User user = userRepository.findByOauthId(oauthId);
        if(user == null)
            throw new IllegalArgumentException("존재하지 않는 사용자");

        log.info("사용자 삭제 시작: 사용자 = {}", oauthId);

        // 1. S3 버킷의 이미지 삭제 (프로필 + 배경화면 이미지)
        awsS3Service.deleteFileByKey(user.getProfileImage());
        awsS3Service.deleteFileByKey(user.getBackImage());

        // 2. Refresh 토큰 삭제
        refreshRepository.deleteAllByOauthId(oauthId);

        // 3. 외부 연동 해제
        try {
            externalOAuthUnlinkService.unlink(user);
        } catch (Exception e) {
            log.error("외부 연동 해제 중 오류(계정 삭제는 계속): {}", e.getMessage(), e);
        }

        // 4. 유저 삭제
        userRepository.delete(user);

    }
}
