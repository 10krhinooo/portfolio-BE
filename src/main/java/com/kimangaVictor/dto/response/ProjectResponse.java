package com.kimangaVictor.dto.response;

import java.util.List;

public record ProjectResponse(
        String slug,
        String title,
        String number,
        String category,
        String description,
        String period,
        List<String> tags,
        String repoUrl,
        String liveUrl,
        String thumbnailUrl,
        boolean featured
) {
}
