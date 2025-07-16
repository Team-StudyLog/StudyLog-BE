package org.example.studylog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.studylog.entity.quiz.Quiz;
import org.example.studylog.entity.user.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Record extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID", insertable = false, updatable = false)
    @Column(nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isQuizCreated;

    @OneToMany(mappedBy = "record")
    private List<Quiz> quizzes = new ArrayList<>();

}
