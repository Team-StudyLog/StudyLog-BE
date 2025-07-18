package org.example.studylog.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileCreateRequestDTO {
    private MultipartFile profileImage;
    private String nickname;
    private String intro;
}
