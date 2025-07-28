package org.example.studylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studylog.entity.category.Category;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String color;

    public static CategoryDTO from(Category category){
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .color(String.valueOf(category.getColor()))
                .build();
    }
}
