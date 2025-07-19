package org.example.studylog.exception.handler;

import org.example.studylog.exception.BusinessException;
import org.example.studylog.exception.TokenValidationException;
import org.example.studylog.exception.UserNotFoundException;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<?> handlerTokenValidationException(TokenValidationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "statusCode", 400,
                        "message", e.getMessage()
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseUtil.buildResponse(404, e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseUtil.buildResponse(400, message, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e){
        return ResponseUtil.buildResponse(e.getErrorCode().getStatus(), e.getErrorCode().getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, Object> response = new HashMap<>();

        // 첫 번째 에러만 반환 - API 명세서 기반
        FieldError firstError = e.getBindingResult().getFieldErrors().get(0);
        String errorMessage = firstError.getDefaultMessage();

        response.put("statusCode", 400);
        response.put("message", "잘못된 접근입니다");
        response.put("data", Map.of("example", errorMessage));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
