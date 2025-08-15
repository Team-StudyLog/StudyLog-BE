package org.example.studylog.dto.studyrecord;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class UpdateStudyRecordRequestDTO {

    @NotNull(message = "카테고리는 필수입니다")
    private Long categoryId;

    @NotBlank(message = "제목은 필수 입력 항목입니다")
    @Size(max = 20, message = "제목은 20자를 초과할 수 없습니다")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다")
    @Size(min = 10, message = "내용은 최소 10자 이상 입력해주세요")
    private String content;
}