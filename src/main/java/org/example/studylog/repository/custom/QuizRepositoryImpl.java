package org.example.studylog.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.studylog.entity.category.QCategory;
import org.example.studylog.entity.quiz.QQuiz;
import org.example.studylog.entity.quiz.Quiz;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class QuizRepositoryImpl implements QuizRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Quiz> findQuizzes(Long userId, Long lastId, int size, LocalDate date, Long categoryId, String query) {

        QQuiz quiz = QQuiz.quiz;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(quiz.user.id.eq(userId));

        if (lastId != null){
            builder.and(quiz.id.lt(lastId));
        }

        if (date != null){
            builder.and(quiz.createdAt.between(
                    date.atStartOfDay(),
                    date.plusDays(1).atStartOfDay()
            ));
        }

        if (categoryId != null){
            builder.and(quiz.category.id.eq(categoryId));
        }

        if (query != null && !query.isBlank()) {
            builder.and(
                    quiz.question.containsIgnoreCase(query));
        }

        return queryFactory.selectFrom(quiz)
                .join(quiz.category, QCategory.category).fetchJoin()
                .where(builder)
                .orderBy(quiz.id.desc())
                .limit(size)
                .fetch();
    }

}
