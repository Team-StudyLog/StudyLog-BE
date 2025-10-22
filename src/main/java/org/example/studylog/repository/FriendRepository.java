package org.example.studylog.repository;


import org.example.studylog.entity.Friend;
import org.example.studylog.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    boolean existsByUserAndFriend(User user, User friend);

    Optional<Friend> findByUserAndFriend(User user, User friend);

    long countByUser(User user);

    @Query("SELECT f.friend.id FROM Friend f WHERE f.user.id = :userId")
    List<Long> findFriendIdsByUserId(Long userId);
}
