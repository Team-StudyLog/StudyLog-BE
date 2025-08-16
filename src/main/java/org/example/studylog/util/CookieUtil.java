package org.example.studylog.util;

import org.springframework.http.ResponseCookie;

public class CookieUtil {
    public static ResponseCookie createCookie(String key, String value){
        return ResponseCookie.from(key, value)
                .httpOnly(true)              // JS 접근 불가
                .path("/")                   // 모든 경로에서 쿠키 전송
                .maxAge(60 * 60 * 60)        // 유효 시간 (초 단위)
//                .secure(true)                // HTTPS에서만 전송
//                .domain(".studylog.shop")    // 도메인 지정 (서브도메인 포함)
//                .sameSite("None")            // 크로스 도메인 쿠키 허용 시 필요
                .build();
    }
}
