package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import org.example.studylog.dto.RankingResponseDTO;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.FriendRepository;
import org.example.studylog.repository.UserRepository;
import org.example.studylog.repository.custom.RankingRepositoryImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final FriendRepository friendRepository;
    private final RankingRepositoryImpl rankingRepository;
    private final UserRepository userRepository;

    public List<RankingResponseDTO> getFriendRankings(
            String oauthId,
            Integer year,
            Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        // 현재 유저 DB에서 조회
        User currentUser = userRepository.findByOauthId(oauthId);

        // 친구 ID 목록에 현재 유저 ID 포함
        List<Long> userIds = new ArrayList<>(friendRepository.findFriendIdsByUserId(currentUser.getId()));
        userIds.add(currentUser.getId());

        List<RankingResponseDTO> rankings = rankingRepository.findFriendRankings(targetYear, targetMonth, userIds);

        // isMe 필드 추가
        rankings.forEach(r -> {
            if (r.getId().equals(currentUser.getId())) {
                r.setMe(true);
            }
        });

        return rankings;
    }
}
