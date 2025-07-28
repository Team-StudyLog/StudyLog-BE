package org.example.studylog.repository.custom;

import org.example.studylog.entity.quiz.Quiz;

import java.time.LocalDate;
import java.util.List;

public interface QuizRepositoryCustom {
    List<Quiz> findQuizzes(Long userId, Long lastId, int size, LocalDate date, Long categoryId, String query);
}
