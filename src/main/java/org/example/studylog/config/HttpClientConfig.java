package org.example.studylog.config;

import org.example.studylog.client.ChatGptClient;
import org.example.studylog.client.GoogleClient;
import org.example.studylog.client.KakaoApiClient;
import org.example.studylog.client.KakaoAuthClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Value("${spring.openai.api-key}")
    private String apiKey;

    @Bean
    public ChatGptClient chatGptClient(){
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(ChatGptClient.class);
    }

    @Bean
    public GoogleClient googleClient(){
        RestClient restClient = RestClient.builder()
                .baseUrl("https://oauth2.googleapis.com")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(GoogleClient.class);

    }

    @Bean
    public KakaoAuthClient kakaoAuthClient(RestClient.Builder builder) {
        RestClient rc = builder
                .baseUrl("https://kauth.kakao.com")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rc))
                .build()
                .createClient(KakaoAuthClient.class);
    }

    @Bean
    public KakaoApiClient kakaoApiClient(RestClient.Builder builder) {
        RestClient rc = builder
                .baseUrl("https://kapi.kakao.com")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rc))
                .build()
                .createClient(KakaoApiClient.class);
    }
}
