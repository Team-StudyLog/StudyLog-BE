package org.example.studylog.entity.user;

import jakarta.persistence.*;
import lombok.*;
import org.example.studylog.entity.StudyRecord;
import org.example.studylog.entity.category.Category;
import org.example.studylog.entity.quiz.Quiz;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(length = 100)
    private String intro;

    @Column(nullable = false)
    private String profileImage;

    private String backImage;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long recordCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean isProfileCompleted;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(length = 5, unique = true)
    private String code;

    @Column(nullable = false, unique = true)
    private String oauthId;

    @OneToMany(mappedBy = "user")
    private List<StudyRecord> records = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Quiz> quizzes = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Category> categories = new ArrayList<>();

    @Column(length = 1000)
    private String refreshToken;

    // 기록 수 증가
    public void incrementRecordCount(){
        this.recordCount++;
    }

    // 기록 수 감소
    public void decrementRecordCount(){
        this.recordCount--;
    }
}
