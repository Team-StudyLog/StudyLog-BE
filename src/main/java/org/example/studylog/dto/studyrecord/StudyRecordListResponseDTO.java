package org.example.studylog.dto.studyrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyRecordListResponseDTO {
    private List<StudyRecordDTO> records;
}
