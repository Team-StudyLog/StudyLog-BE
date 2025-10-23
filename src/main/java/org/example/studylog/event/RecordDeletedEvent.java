package org.example.studylog.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecordDeletedEvent {
    private final Long userId;
    private final int year;
    private final int month;
}
