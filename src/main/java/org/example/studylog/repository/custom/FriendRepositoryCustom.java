package org.example.studylog.repository.custom;

import org.example.studylog.dto.friend.FriendResponseDTO;
import org.example.studylog.entity.user.User;

import java.util.List;

public interface FriendRepositoryCustom {
    List<FriendResponseDTO> findFriendListByUser(User user);
    List<FriendResponseDTO> findFriendListByNickname(User user, String query);
}
