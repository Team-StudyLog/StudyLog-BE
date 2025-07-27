package org.example.studylog.dto.studyrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studylog.dto.CategoryDTO;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyRecordDTO {
    private Long id;
    private String title;
    private String content;
    private CategoryDTO category;
    private String createdAt; // "2025-06-24" 형식
    private Boolean hasQuiz;  // API 명세서에 맞춰 hasQuiz로 유지 (내부는 isQuizCreated)
}
