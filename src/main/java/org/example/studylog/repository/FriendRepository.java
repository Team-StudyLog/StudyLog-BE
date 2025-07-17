package org.example.studylog.repository;


import org.example.studylog.entity.Friend;
import org.example.studylog.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    boolean existsByUserAndFriend(User user, User friend);
}
