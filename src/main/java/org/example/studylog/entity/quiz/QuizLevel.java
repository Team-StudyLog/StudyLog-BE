package org.example.studylog.entity.quiz;

import lombok.Getter;

@Getter
public enum QuizLevel {
    EASY("하"),
    MEDIUM("중"),
    HARD("상");

    private final String label;

    QuizLevel(String label){
        this.label = label;
    }

    public static QuizLevel fromLabel(String label){
        for (QuizLevel level : values()){
            if(level.label.equals(label)) return level;
        }
        throw new IllegalArgumentException("유효하지 않은 난이도입니다.");
    }
}
