package org.example.studylog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Quiz extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType type;

    @ManyToOne
    @JoinColumn(name = "RECORD_ID", insertable = false, updatable = false)
    @Column(nullable = false)
    private Record record;

}
