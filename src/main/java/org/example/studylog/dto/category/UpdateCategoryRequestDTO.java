package org.example.studylog.dto.category;

import lombok.Getter;
import lombok.Setter;
import org.example.studylog.entity.category.Color;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class UpdateCategoryRequestDTO {

    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(max = 10, message = "카테고리 이름은 10자를 초과할 수 없습니다")
    private String name;

    @NotNull(message = "색상은 필수입니다")
    private Color color;
}