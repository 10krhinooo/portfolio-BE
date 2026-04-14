package com.kimangaVictor.controller;

import com.kimangaVictor.service.TelegramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    @Mock
    private TelegramService telegramService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ContactController(telegramService)).build();
    }

    @Test
    void contact_validRequest_returns200AndCallsService() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"message\":\"Hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Message sent."));

        verify(telegramService).sendMessage("Alice", "alice@example.com", "Hello");
    }

    @Test
    void contact_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \",\"email\":\"alice@example.com\",\"message\":\"Hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("All fields are required."));

        verifyNoInteractions(telegramService);
    }

    @Test
    void contact_blankEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"\",\"message\":\"Hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("All fields are required."));

        verifyNoInteractions(telegramService);
    }

    @Test
    void contact_blankMessage_returns400() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"message\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("All fields are required."));

        verifyNoInteractions(telegramService);
    }
}
