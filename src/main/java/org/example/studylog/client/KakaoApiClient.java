package org.example.studylog.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface KakaoApiClient {

    @PostExchange(url = "/v1/user/unlink", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void unlink(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                @RequestBody MultiValueMap<String, String> form);
}
