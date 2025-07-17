package org.example.studylog.dto.friend;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FriendRequestDTO {
    @NotBlank(message = "코드는 필수입니다.")
    private String code;
}
