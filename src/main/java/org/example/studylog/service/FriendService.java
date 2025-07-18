package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import org.example.studylog.dto.friend.FriendNameDTO;
import org.example.studylog.dto.friend.FriendRequestDTO;
import org.example.studylog.dto.friend.FriendResponseDTO;
import org.example.studylog.entity.Friend;
import org.example.studylog.entity.user.User;
import org.example.studylog.exception.BusinessException;
import org.example.studylog.exception.ErrorCode;
import org.example.studylog.repository.FriendRepository;
import org.example.studylog.repository.UserRepository;
import org.example.studylog.repository.custom.FriendRepositoryImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRepositoryImpl friendRepositoryImpl;

    @Transactional(readOnly = true)
    public FriendNameDTO findUserByCode(String code) {
        // 코드로 사용자 조회
        User user = userRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CODE_NOT_FOUND));

        return FriendNameDTO.builder()
                .nickname(user.getNickname())
                .build();
    }

    @Transactional(readOnly = true)
    public List<FriendResponseDTO> getFriendList(String oauthId) {
        // 로그인한 유저 찾기
        User user = userRepository.findByOauthId(oauthId);

        return friendRepositoryImpl.findFriendListByUser(user);
    }

    @Transactional(readOnly = true)
    public List<FriendResponseDTO> getFriendByQuery(String oauthId, String query) {
        // 로그인한 유저 찾기
        User user = userRepository.findByOauthId(oauthId);

        return friendRepositoryImpl.findFriendListByNickname(user, query);
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

    @Transactional
    public FriendResponseDTO deleteFriend(String oauthId, Long friendId) {
        // 로그인한 유저 찾기
        User user = userRepository.findByOauthId(oauthId);
        // 삭제할 친구 찾기
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_NOT_FOUND));

        // 친구 관계가 맞는지 확인
        boolean alreadyAdded = friendRepository.existsByUserAndFriend(user, friend);
        if(!alreadyAdded){
            throw new BusinessException(ErrorCode.NOT_FRIEND);
        }

        Friend toFriend = friendRepository.findByUserAndFriend(user, friend)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FRIEND));
        Friend fromFriend = friendRepository.findByUserAndFriend(friend, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FRIEND));

        friendRepository.delete(toFriend);
        friendRepository.delete(fromFriend);

        return new FriendResponseDTO(friend.getId(), friend.getNickname(), friend.getProfileImage());
    }
}
