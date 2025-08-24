package org.example.studylog.client;

import com.nimbusds.oauth2.sdk.TokenResponse;
import org.example.studylog.dto.oauth.OAuthTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface KakaoAuthClient {

    // POST /oauth/token  (x-www-form-urlencoded)
    @PostExchange(url = "/oauth/token",
            contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    OAuthTokenResponse refreshToken(@RequestBody MultiValueMap<String, String> form);
}
