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
public class StudyRecordDetailDTO {
    private Long id;
    private String title;
    private String content;
    private CategoryDTO category;
    private String createdAt;   // "2025-06-24"
    private Integer quizCount;  // 퀴즈 개수
}
