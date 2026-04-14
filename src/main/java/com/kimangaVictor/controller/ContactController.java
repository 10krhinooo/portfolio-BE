package com.kimangaVictor.controller;

import com.kimangaVictor.dto.ContactRequest;
import com.kimangaVictor.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://10krhinooo.github.io/")
public class ContactController {

    private final TelegramService telegramService;

    @PostMapping
    public ResponseEntity<String> contact(@RequestBody ContactRequest req) {
        if (req.getName().isBlank() || req.getEmail().isBlank() || req.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body("All fields are required.");
        }
        telegramService.sendMessage(req.getName(), req.getEmail(), req.getMessage());
        return ResponseEntity.ok("Message sent.");
    }
}

