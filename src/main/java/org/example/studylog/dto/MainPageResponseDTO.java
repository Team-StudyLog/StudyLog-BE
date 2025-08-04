package org.example.studylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studylog.dto.friend.FriendResponseDTO;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainPageResponseDTO {

    private List<FriendResponseDTO> following;
    private ProfileDTO profile;
    private StreakDTO streak;
    private List<CategoryCountDTO> categories;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileDTO {
        private String coverImage;
        private String profileImage;
        private String name;
        private String intro;
        private Integer level;
        private String code;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreakDTO {
        private Integer maxStreak;
        private Map<String, Integer> recordCountPerDay; // currentStreak → recordCountPerDay로 변경
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryCountDTO {
        private String name;
        private Integer count;
    }
}