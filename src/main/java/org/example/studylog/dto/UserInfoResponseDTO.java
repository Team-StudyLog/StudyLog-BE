package org.example.studylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponseDTO {
    private String profileImage;
    private String nickname;
    private String intro;
    private Long friendCount;
    private String code;
}
