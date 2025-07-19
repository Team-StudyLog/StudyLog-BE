package org.example.studylog.repository;

import org.example.studylog.entity.Streak;
import org.example.studylog.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Long> {

    // 사용자의 스트릭 정보 조회
    Optional<Streak> findByUser(User user);
}