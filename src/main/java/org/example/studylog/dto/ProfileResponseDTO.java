package org.example.studylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class ProfileResponseDTO {
    private String profileImage;
    private String nickname;
    private String intro;
}
