package org.example.studylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakDTO {
    private Integer currentStreak;
    private Boolean isStreakUpdated;
}
