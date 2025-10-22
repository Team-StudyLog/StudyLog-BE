package org.example.studylog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RankingResponseDTO {
    private Long id;
    private String nickname;
    private String profileImage;
    private String code;
    private int recordCount;
    private boolean isMe;
}
