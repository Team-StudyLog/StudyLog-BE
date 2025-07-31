package org.example.studylog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

public class BackgroundDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestDTO {
        @NotNull(message = "배경화면 이미지는 필수입니다.")
        private MultipartFile coverImage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDTO {
        private String coverImage;
    }

}
