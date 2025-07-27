package org.example.studylog.dto.quiz;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.studylog.entity.quiz.QuizLevel;

@Getter
@Setter
public class CreateQuizRequestDTO {

    @NotNull(message = "난이도는 필수입니다")
    private QuizLevel level;

    @Min(value = 1, message = "퀴즈 갯수는 1 이상이어야 합니다")
    private int quizCount;

    private String requirement;
}
