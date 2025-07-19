package org.example.studylog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileCreateRequestDTO {
    @NotNull(message = "사진은 필수입니다.")
    private MultipartFile profileImage;
    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;
    @NotBlank(message = "한줄 소개는 필수입니다.")
    private String intro;
}
