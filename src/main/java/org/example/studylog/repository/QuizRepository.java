package org.example.studylog.repository;

import org.example.studylog.entity.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;


public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
