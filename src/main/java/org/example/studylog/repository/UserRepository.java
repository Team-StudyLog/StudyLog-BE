package org.example.studylog.repository;

import org.example.studylog.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByOauthId(String oauthId);

    boolean existsByCode(String code);

    Optional<User> findByCode(String code);
}
