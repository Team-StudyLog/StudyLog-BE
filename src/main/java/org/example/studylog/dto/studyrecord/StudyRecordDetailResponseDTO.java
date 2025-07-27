package org.example.studylog.dto.studyrecord;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studylog.dto.QuizDTO;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyRecordDetailResponseDTO {
    private StudyRecordDetailDTO record;
    private List<QuizDTO> quizzes;
}
