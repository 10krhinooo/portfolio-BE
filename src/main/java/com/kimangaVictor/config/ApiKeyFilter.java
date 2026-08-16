package com.kimangaVictor.config;

import tools.jackson.databind.json.JsonMapper;
import com.kimangaVictor.dto.response.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/**
 * Guards {@code /api/admin/**} with a shared secret in the {@code X-API-Key} header.
 *
 * <p>Spring Security would be the usual answer, but the only thing being protected is one
 * read-only stats endpoint for a single operator. A filter keeps the dependency surface small.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String ADMIN_PREFIX = "/api/admin";
    private static final String HEADER = "X-API-Key";

    private final PortfolioProperties properties;
    private final JsonMapper jsonMapper;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().startsWith(ADMIN_PREFIX);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String configured = properties.admin().apiKey();
        String supplied = request.getHeader(HEADER);

        if (configured == null || configured.isBlank() || !constantTimeEquals(configured, supplied)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            jsonMapper.writeValue(response.getOutputStream(),
                    new ApiError("unauthorized", "A valid " + HEADER + " header is required.", Map.of()));
            return;
        }
        chain.doFilter(request, response);
    }

    /** Avoids leaking key length or a matching prefix through response timing. */
    private boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
