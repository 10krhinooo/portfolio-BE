# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run (port 8083)
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=KimangaVcitorApplicationTests
```

## Architecture

This is a minimal Spring Boot 4.0.5 (Java 21) backend for a personal portfolio contact form. It has a single responsibility: receive contact form submissions via HTTP and forward them to a Telegram chat.

**Request flow:**
`POST /api/contact` → `ContactController` → `TelegramService` → Telegram Bot API

- **`ContactController`** — validates that name, email, and message are non-blank, then delegates to `TelegramService`. CORS is restricted to `https://10krhinooo.github.io/` (the portfolio frontend).
- **`TelegramService`** — formats the message in Telegram's MarkdownV2 syntax (with special-character escaping) and POSTs to `https://api.telegram.org/bot{token}/sendMessage` using a plain `RestTemplate`.
- **`ContactRequest`** — Lombok `@Data` DTO with three string fields.

## Configuration

`application.properties` holds all runtime config. The required properties are:

```
server.port=8083
telegram.bot-token=<bot token>
telegram.chat-id=<chat id>
```

Both `telegram.*` values are injected into `TelegramService` via `@Value`. There is no database, no security layer, and no additional Spring profiles.
