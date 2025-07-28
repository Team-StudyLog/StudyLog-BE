package org.example.studylog.dto.quiz;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.studylog.entity.quiz.Quiz;

@Getter
@Setter
@Builder
public class QuizSummaryDTO {
    private Long id;
    private String category;
    private String color;
    private String question;
    private String level;

    public static QuizSummaryDTO from(Quiz quiz) {
        return QuizSummaryDTO.builder()
                .id(quiz.getId())
                .category(quiz.getCategory().getName())
                .color(String.valueOf(quiz.getCategory().getColor()))
                .question(quiz.getQuestion())
                .level(quiz.getLevel().getLabel())
                .build();
    }
}
