package org.example.studylog.service.oauth;

import lombok.RequiredArgsConstructor;
import org.example.studylog.client.GoogleClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final GoogleClient googleClient;

    public void revoke(String token) {
        googleClient.unlink(token);
    }
}
