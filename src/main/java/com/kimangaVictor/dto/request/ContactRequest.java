package com.kimangaVictor.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param website honeypot — a hidden field real users never fill. Bots that autofill every input
 *                trip it, and the endpoint then returns a normal 200 without notifying anyone.
 */
public record ContactRequest(

        @NotBlank(message = "Please tell me your name.")
        @Size(max = 120, message = "That name is too long.")
        String name,

        @NotBlank(message = "Please enter an email address.")
        @Email(message = "That does not look like a valid email address.")
        @Size(max = 200, message = "That email address is too long.")
        String email,

        @NotBlank(message = "Please write a message.")
        @Size(max = 4000, message = "Please keep the message under 4000 characters.")
        String message,

        String website
) {
    public boolean isBot() {
        return website != null && !website.isBlank();
    }
}
