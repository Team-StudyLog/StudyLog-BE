package org.example.studylog.dto.studyrecord;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyRecordFilterRequestDTO {
    private Long categoryId;    // 선택적
    private String date;        // "YYYY-MM-DD" 형식, 선택적
    private Long lastId;        // 무한 스크롤용, 선택적
    private Integer size = 10;  // 기본 페이지 크기
}