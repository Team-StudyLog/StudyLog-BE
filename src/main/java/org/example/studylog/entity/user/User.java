package org.example.studylog.entity.user;

import jakarta.persistence.*;
import lombok.*;

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
}
