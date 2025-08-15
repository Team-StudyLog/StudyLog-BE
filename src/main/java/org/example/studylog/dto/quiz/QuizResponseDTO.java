package org.example.studylog.dto.quiz;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.studylog.dto.CategoryDTO;
import org.example.studylog.entity.category.Category;
import org.example.studylog.entity.quiz.Quiz;

import java.time.format.DateTimeFormatter;

@Setter
@Getter
@Builder
public class QuizResponseDTO {

    private String createdAt;
    private String type;
    private CategoryDTO category;
    private String question;
    private String answer;
    private String level;
    private Long recordId;

    public static QuizResponseDTO from(Quiz quiz, Category category){

        return QuizResponseDTO.builder()
                .createdAt(quiz.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .type(String.valueOf(quiz.getType()))
                .category(CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .color(String.valueOf(category.getColor()))
                        .build())
                .question(quiz.getQuestion())
                .answer(quiz.getAnswer())
                .level(String.valueOf(quiz.getLevel()))
                .recordId(quiz.getRecord().getId())
                .build();
    }
}
