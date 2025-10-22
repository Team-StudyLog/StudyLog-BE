package org.example.studylog.repository.custom;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studylog.dto.RankingResponseDTO;
import org.example.studylog.entity.QUserMonthlyStat;
import org.example.studylog.entity.UserMonthlyStat;
import org.example.studylog.entity.user.QUser;
import org.example.studylog.entity.user.User;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RankingResponseDTO> findFriendRankings(
            int year,
            int month,
            List<Long> userIds
    ) {
        QUserMonthlyStat stat = QUserMonthlyStat.userMonthlyStat;
        QUser user = QUser.user;

        return queryFactory
                .select(Projections.constructor(
                        RankingResponseDTO.class,
                        user.id,
                        user.nickname,
                        user.profileImage,
                        user.code,
                        stat.recordCount,
                        Expressions.constant(false)
                ))
                .from(stat)
                .join(stat.user, user)
                .where(
                        user.id.in(userIds),
                        stat.year.eq(year),
                        stat.month.eq(month)
                )
                .orderBy(stat.recordCount.desc(), user.id.asc())
                .fetch();
    }

    @Override
    @Transactional
    public void incrementOrInsert(Long userId, int year, int month) {
        QUserMonthlyStat stat = QUserMonthlyStat.userMonthlyStat;

        // 이미 존재하는지 확인
        UserMonthlyStat existing = queryFactory
                .selectFrom(stat)
                .where(
                        stat.user.id.eq(userId),
                        stat.year.eq(year),
                        stat.month.eq(month)
                )
                .fetchOne();

        if (existing != null) {
            //있으면 recordCount + 1
            queryFactory.update(stat)
                    .set(stat.recordCount, stat.recordCount.add(1))
                    .where(stat.id.eq(existing.getId()))
                    .execute();
        } else {
            // 없으면 새로 insert
            UserMonthlyStat newStat = UserMonthlyStat.builder()
                    .user(User.builder().id(userId).build()) // FK만 세팅
                    .year(year)
                    .month(month)
                    .recordCount(1)
                    .build();

            queryFactory.insert(stat)
                    .columns(stat.user, stat.year, stat.month, stat.recordCount)
                    .values(newStat.getUser(), newStat.getYear(), newStat.getMonth(), newStat.getRecordCount())
                    .execute();
        }
    }
}
