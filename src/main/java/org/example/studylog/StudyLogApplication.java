package org.example.studylog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class StudyLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyLogApplication.class, args);
    }

}
