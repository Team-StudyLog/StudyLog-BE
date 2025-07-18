package org.example.studylog.repository.custom;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.studylog.dto.friend.FriendResponseDTO;
import org.example.studylog.entity.QFriend;
import org.example.studylog.entity.user.QUser;
import org.example.studylog.entity.user.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendRepositoryImpl implements FriendRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FriendResponseDTO> findFriendListByUser(User user) {
        QFriend friend = QFriend.friend1;

        return queryFactory
                .select(Projections.constructor(FriendResponseDTO.class,
                        friend.friend.id,
                        friend.friend.nickname,
                        friend.friend.profileImage))
                .from(friend)
                .where(friend.user.eq(user))
                .fetch();
    }

    @Override
    public List<FriendResponseDTO> findFriendListByNickname(User user, String query) {
        QFriend friend = QFriend.friend1;

        return queryFactory
                .select(Projections.constructor(FriendResponseDTO.class,
                        friend.friend.id,
                        friend.friend.nickname,
                        friend.friend.profileImage))
                .from(friend)
                .where(friend.user.eq(user),
                        friend.friend.nickname.containsIgnoreCase(query))
                .fetch();
    }
}
