package com.kimangaVictor.dto.response;

import java.time.Instant;
import java.util.List;

public record GithubRepoResponse(
        String name,
        String description,
        String language,
        List<String> topics,
        int stars,
        int forks,
        String url,
        String homepage,
        Instant pushedAt
) {
}
