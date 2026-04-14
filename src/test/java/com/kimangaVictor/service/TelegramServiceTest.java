package com.kimangaVictor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramServiceTest {

    private TelegramService telegramService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        telegramService = new TelegramService();
        ReflectionTestUtils.setField(telegramService, "botToken", "test-token");
        ReflectionTestUtils.setField(telegramService, "chatId", "123456789");
        ReflectionTestUtils.setField(telegramService, "restTemplate", restTemplate);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{}"));
    }

    @Test
    void sendMessage_usesTokenInUrl() {
        telegramService.sendMessage("Alice", "alice@example.com", "Hello");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(), eq(String.class));
        assertThat(urlCaptor.getValue()).contains("test-token");
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendMessage_bodyContainsAllFields() {
        telegramService.sendMessage("Bob", "bob@example.com", "Test message");

        ArgumentCaptor<HttpEntity<Map<String, String>>> bodyCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), bodyCaptor.capture(), eq(String.class));

        String text = bodyCaptor.getValue().getBody().get("text");
        assertThat(text).contains("Bob");
        assertThat(text).contains("bob@example\\.com"); // dot is escaped in MarkdownV2
        assertThat(text).contains("Test message");
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendMessage_escapesSpecialCharsForMarkdownV2() {
        telegramService.sendMessage("Test_User", "user@test.com", "Hi!");

        ArgumentCaptor<HttpEntity<Map<String, String>>> bodyCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), bodyCaptor.capture(), eq(String.class));

        String text = bodyCaptor.getValue().getBody().get("text");
        assertThat(text).contains("Test\\_User");   // underscore escaped
        assertThat(text).contains("Hi\\!");         // exclamation mark escaped
    }
}
