package org.example.studylog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {
    private int statusCode;
    private String message;
    private T data;
}
