package com.kimangaVictor.controller;

import com.kimangaVictor.config.PortfolioProperties;
import com.kimangaVictor.exception.GlobalExceptionHandler;
import com.kimangaVictor.service.RateLimitService;
import com.kimangaVictor.service.TelegramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private static final int MAX_PER_HOUR = 3;

    @Mock
    private TelegramService telegramService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PortfolioProperties properties = new PortfolioProperties(
                new PortfolioProperties.Cors(List.of("https://10krhinooo.github.io")),
                new PortfolioProperties.Contact(MAX_PER_HOUR, 4000),
                new PortfolioProperties.Github("10krhinooo", "", List.of()),
                new PortfolioProperties.Admin("test-admin-key"),
                new PortfolioProperties.Analytics("test-salt", 60),
                new PortfolioProperties.Seed(false));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ContactController(telegramService, new RateLimitService(properties)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static String body(String name, String email, String message) {
        return body(name, email, message, "");
    }

    private static String body(String name, String email, String message, String website) {
        return """
                {"name":"%s","email":"%s","message":"%s","website":"%s"}
                """.formatted(name, email, message, website);
    }

    @Test
    void validRequest_returns200AndForwardsToTelegram() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "alice@example.com", "Hello")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("sent"));

        verify(telegramService).sendMessage("Alice", "alice@example.com", "Hello");
    }

    @Test
    void blankName_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ", "alice@example.com", "Hello")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.fields.name").exists());

        verifyNoInteractions(telegramService);
    }

    @Test
    void malformedEmail_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "not-an-email", "Hello")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());

        verifyNoInteractions(telegramService);
    }

    @Test
    void blankMessage_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "alice@example.com", "  ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.message").exists());

        verifyNoInteractions(telegramService);
    }

    @Test
    void filledHoneypot_looksSuccessfulButNotifiesNobody() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Bot", "bot@example.com", "Buy things", "http://spam.example")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("sent"));

        verifyNoInteractions(telegramService);
    }

    @Test
    void exceedingHourlyLimit_returns429() throws Exception {
        for (int i = 0; i < MAX_PER_HOUR; i++) {
            mockMvc.perform(post("/api/contact")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body("Alice", "alice@example.com", "Hello " + i)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "alice@example.com", "One too many")))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("rate_limited"));
    }
}
