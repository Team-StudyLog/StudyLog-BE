package org.example.studylog.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {
    public static Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
//        cookie.setSecure(true);

        return cookie;
    }
}
