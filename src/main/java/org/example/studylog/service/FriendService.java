package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import org.example.studylog.dto.friend.FriendNameDTO;
import org.example.studylog.entity.user.User;
import org.example.studylog.exception.UserNotFoundException;
import org.example.studylog.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;

    public FriendNameDTO findUserByCode(String code) {
        // 코드로 사용자 조회
        User user = userRepository.findByCode(code)
                .orElseThrow(() -> new UserNotFoundException("코드에 해당하는 사용자 없음"));

        return FriendNameDTO.builder()
                .nickname(user.getNickname())
                .build();
    }
}
