package org.example.studylog.client;

import org.example.studylog.dto.quiz.chatGPT.ChatGptRequest;
import org.example.studylog.dto.quiz.chatGPT.ChatGptResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/chat/completions")
public interface ChatGptClient {

    @PostExchange
    ChatGptResponse getChatCompletions(@RequestBody ChatGptRequest request);
}
