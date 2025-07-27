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
public class StudyRecordFilterResponseDTO {
    private List<StudyRecordDTO> records;
    private Boolean hasMore;     // 더 많은 데이터가 있는지
    private Long nextLastId;     // 다음 요청에 사용할 lastId
}

