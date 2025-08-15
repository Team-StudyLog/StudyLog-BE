package org.example.studylog.entity.category;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.studylog.entity.StudyRecord;
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
public class Category {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 10)
    private String name;

    @Enumerated(EnumType.STRING)
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "category")
    private List<StudyRecord> records = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    private List<Quiz> quizzes = new ArrayList<>();
}
