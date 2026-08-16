package com.kimangaVictor.exception;

/** Typed failures the controllers can raise, mapped to status codes by the global handler. */
public final class ApiExceptions {

    private ApiExceptions() {
    }

    /** Client exceeded a token bucket — mapped to 429. */
    public static class RateLimitedException extends RuntimeException {
        public RateLimitedException(String message) {
            super(message);
        }
    }

    /** An upstream dependency (Telegram, GitHub) failed — mapped to 502. */
    public static class UpstreamException extends RuntimeException {
        public UpstreamException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** Missing or wrong admin API key — mapped to 401. */
    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
