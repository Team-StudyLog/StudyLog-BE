package org.example.studylog.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.studylog.entity.user.User;

@Getter
@AllArgsConstructor
public class RecordEvent {

    private final User user;

}
