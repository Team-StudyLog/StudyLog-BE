package org.example.studylog.dto.studyrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studylog.dto.StreakDTO;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudyRecordResponseDTO {
    private StudyRecordDTO record;
    private StreakDTO streak;
}
