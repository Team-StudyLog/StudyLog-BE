package org.example.studylog.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studylog.dto.friend.FriendNameDTO;
import org.example.studylog.dto.friend.FriendRequestDTO;
import org.example.studylog.entity.Friend;
import org.example.studylog.entity.user.User;
import org.example.studylog.exception.BusinessException;
import org.example.studylog.exception.ErrorCode;
import org.example.studylog.exception.UserNotFoundException;
import org.example.studylog.repository.FriendRepository;
import org.example.studylog.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public FriendNameDTO findUserByCode(String code) {
        // 코드로 사용자 조회
        User user = userRepository.findByCode(code)
                .orElseThrow(() -> new UserNotFoundException("코드에 해당하는 사용자 없음"));

        return FriendNameDTO.builder()
                .nickname(user.getNickname())
                .build();
    }

    @Transactional
    public void addFriend(FriendRequestDTO request, String oauthId) {
        // 로그인한 유저 찾기
        User user = userRepository.findByOauthId(oauthId);
        // 친구할 유저 찾기
        User friend = userRepository.findByCode(request.getCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CODE_NOT_FOUND));

        // 이미 친구인지 확인
        boolean alreadyAdded = friendRepository.existsByUserAndFriend(user, friend);
        if(alreadyAdded){
            throw new BusinessException(ErrorCode.ALREADY_FRIEND);
        }

        // 단방향 관계 2개 설정 (양방향)
        Friend toFriend = Friend.builder()
                .user(user)
                .friend(friend)
                .build();
        Friend fromFriend = Friend.builder()
                .user(friend)
                .friend(user)
                .build();

        friendRepository.save(toFriend);
        friendRepository.save(fromFriend);
    }
}
