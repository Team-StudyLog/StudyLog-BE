package org.example.studylog.repository;

import org.example.studylog.entity.category.Category;
import org.example.studylog.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 사용자의 카테고리를 이름순으로 조회
    List<Category> findByUserOrderByNameAsc(User user);

    // 특정 ID와 사용자로 카테고리 조회 (권한 확인용)
    Optional<Category> findByIdAndUser(Long id, User user);

    // 사용자가 동일한 이름의 카테고리를 가지고 있는지 확인
    boolean existsByNameAndUser(String name, User user);
}