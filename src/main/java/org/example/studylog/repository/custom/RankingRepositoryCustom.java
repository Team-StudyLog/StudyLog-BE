package org.example.studylog.repository.custom;

import org.example.studylog.dto.RankingResponseDTO;

import java.util.List;

public interface RankingRepositoryCustom {
    List<RankingResponseDTO> findFriendRankings(int year, int month, List<Long> userIds);
    void incrementOrInsert(Long userId, int year, int month);
}
