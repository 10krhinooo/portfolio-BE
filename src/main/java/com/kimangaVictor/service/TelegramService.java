package com.kimangaVictor.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;


@Service
public class TelegramService {
    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String name, String email, String message) {
        String timestamp = java.time.ZonedDateTime.now(java.time.ZoneId.of("Africa/Nairobi"))
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a z"));
        String text = String.format(
                "📬 *New Portfolio Message*\n\n" +
                        " *Name:* %s\n" +
                        " *Email:* %s\n\n" +
                        " *Message:*\n%s" +
                        " *Received:* %s ",
                escape(name), escape(email), escape(message), escape(timestamp)
        );

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "chat_id", chatId,
                "text", text,
                "parse_mode", "MarkdownV2"
        );

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    // MarkdownV2 requires escaping special chars
    private String escape(String text) {
        return text.replaceAll("([_*\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }
}
