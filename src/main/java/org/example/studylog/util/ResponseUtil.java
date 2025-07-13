package org.example.studylog.util;

import org.example.studylog.dto.ResponseDTO;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {
    public static <T> ResponseEntity<ResponseDTO<T>> buildResponse(int statusCode, String message, T data) {
        return ResponseEntity.status(statusCode)
                .body(new ResponseDTO<>(statusCode, message, data));
    }
}
