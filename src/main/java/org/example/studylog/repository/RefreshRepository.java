package org.example.studylog.repository;

import jakarta.transaction.Transactional;
import org.example.studylog.entity.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {

    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);

    void deleteAllByOauthId(String oauthId);
}
