package org.example.studylog.event;

import org.example.studylog.entity.user.User;

public class RecordCreatedEvent {
    private final User user;

    public RecordCreatedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
