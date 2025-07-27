package org.example.studylog.dto.quiz.chatGPT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptRequestDTO {

    private String model = "gpt-3.5-turbo";
    private List<Message> messages;
    private double temperature = 0.7;

    
}
