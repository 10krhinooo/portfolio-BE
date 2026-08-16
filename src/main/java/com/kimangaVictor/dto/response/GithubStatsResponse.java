package com.kimangaVictor.dto.response;

import java.util.List;
import java.util.Map;

/**
 * @param languages repo count per language, highest first
 * @param topRepos  the handful of most recently pushed repos, for the activity strip
 */
public record GithubStatsResponse(
        int publicRepos,
        int totalStars,
        Map<String, Integer> languages,
        List<GithubRepoResponse> topRepos
) {
}
