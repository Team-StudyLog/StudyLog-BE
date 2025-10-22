package org.example.studylog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.studylog.entity.user.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_monthly_stat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "year", "month"}))
public class UserMonthlyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int year;
    private int month;

    @Column(nullable = false)
    private int recordCount;
}
