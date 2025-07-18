package org.example.studylog.repository;


import org.example.studylog.entity.Friend;
import org.example.studylog.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    boolean existsByUserAndFriend(User user, User friend);

    Optional<Friend> findByUserAndFriend(User user, User friend);

    long countByUser(User user);
}
