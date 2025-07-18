package org.example.studylog.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class FriendResponseDTO {
    private Long id;
    private String nickname;
    private String profileImage;
    private String code;
}
