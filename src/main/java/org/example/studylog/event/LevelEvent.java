package org.example.studylog.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.studylog.entity.user.User;

@Getter
@AllArgsConstructor
public class LevelEvent {

    private final User user;
    private final int newLevel;
    private final ActionType action;

    public enum ActionType{
        UP, DOWN
    }
}
