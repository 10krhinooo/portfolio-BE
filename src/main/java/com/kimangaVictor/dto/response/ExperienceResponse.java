package com.kimangaVictor.dto.response;

import java.util.List;

public record ExperienceResponse(
        String slug,
        String role,
        String organisation,
        String location,
        String period,
        String kind,
        List<String> bullets
) {
}
