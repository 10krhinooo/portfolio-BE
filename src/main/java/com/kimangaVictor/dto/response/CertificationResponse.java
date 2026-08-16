package com.kimangaVictor.dto.response;

public record CertificationResponse(
        String slug,
        String title,
        String issuer,
        String awarded,
        String credentialUrl
) {
}
