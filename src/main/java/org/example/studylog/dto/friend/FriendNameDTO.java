package org.example.studylog.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class FriendNameDTO {
    private String nickname;
}
