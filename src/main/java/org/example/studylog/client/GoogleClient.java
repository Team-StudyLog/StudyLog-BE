package org.example.studylog.client;

import org.example.studylog.dto.oauth.OAuthTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface GoogleClient {

    // Refresh 토큰으로 액세스 토큰 재발급
    @PostExchange(url = "/token", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    OAuthTokenResponse reissueToken(@RequestBody MultiValueMap<String, String> form);

    // 토큰 리보크(연결 해제)
    @PostExchange(url = "/revoke", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void unlink(@RequestParam String token);

}
