package org.example.studylog.dto.quiz;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.studylog.dto.CategoryDTO;

import java.util.List;

@Getter
@Setter
@Builder
public class QuizListResponseDTO {

    private List<CategoryDTO> categories;
    private List<QuizSummaryDTO> quizzes;
    private boolean hasNext;
    private Long lastId;
}
