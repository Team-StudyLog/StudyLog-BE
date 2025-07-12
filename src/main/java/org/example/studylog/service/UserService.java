package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import org.example.studylog.dto.ProfileRequestDTO;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;

    public void updateUserProfile(ProfileRequestDTO request, String oauthId){
        // 유저 찾기
        User user = userRepository.findByOauthId(oauthId);

        MultipartFile file = request.getProfileImage();
        // S3 업로드
        String imageUrl = awsS3Service.uploadFile(file);

        // User 엔티티에 프로필 정보 업데이트
        user.setNickname(request.getNickname());
        user.setIntro(request.getIntro());
        user.setProfileImage(imageUrl);

        // 프로필 정보 업데이트 상태 바꾸기
        user.setProfileCompleted(true);

        userRepository.save(user);
    }
}
