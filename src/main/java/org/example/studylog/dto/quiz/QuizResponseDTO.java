package org.example.studylog.dto.quiz;

import lombok.Getter;
import lombok.Setter;
import org.example.studylog.dto.CategoryDTO;

@Setter
@Getter
public class QuizResponseDTO {

    private String createdAt;
    private String type;
    private CategoryDTO category;
    private String question;
    private String answer;
    private String level;
    private Long recordId;
}
