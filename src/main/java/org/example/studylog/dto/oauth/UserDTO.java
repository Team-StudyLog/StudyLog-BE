package org.example.studylog.dto.oauth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private String role;
    private String nickname;
    private String oauthId;
    private boolean isProfileCompleted;

}
