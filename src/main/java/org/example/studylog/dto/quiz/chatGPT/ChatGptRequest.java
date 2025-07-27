package org.example.studylog.dto.quiz.chatGPT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptRequest {

    private String model = "gpt-3.5-turbo";
    private List<Message> messages;
    private double temperature = 0.7;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role; // "user"
        private String content;
    }
}
