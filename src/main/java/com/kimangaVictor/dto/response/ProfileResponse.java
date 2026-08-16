package com.kimangaVictor.dto.response;

public record ProfileResponse(
        String firstName,
        String lastName,
        String title,
        String location,
        String tagline,
        String email,
        String phone,
        String githubUrl,
        String linkedinUrl,
        String resumeUrl
) {
}
