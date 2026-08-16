package com.kimangaVictor.service;

import tools.jackson.databind.JsonNode;
import com.kimangaVictor.config.CacheConfig;
import com.kimangaVictor.config.PortfolioProperties;
import com.kimangaVictor.dto.response.GithubRepoResponse;
import com.kimangaVictor.dto.response.GithubStatsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Server-side proxy for the public GitHub API.
 *
 * <p>Calling GitHub from the browser would burn the caller's unauthenticated 60 requests/hour quota
 * and expose no token. Proxying here lets one optional PAT serve every visitor at 5000/hour, and
 * responses are cached for an hour on top of that.
 *
 * <p>If GitHub is unreachable the last successful response is served instead of an error, so the
 * projects section never goes blank because of an upstream blip.
 */
@Service
public class GithubService {

    private static final Logger log = LoggerFactory.getLogger(GithubService.class);
    private static final String API_ROOT = "https://api.github.com";

    private final PortfolioProperties properties;
    private final RestClient restClient;
    private final AtomicReference<List<GithubRepoResponse>> lastGoodRepos =
            new AtomicReference<>(List.of());

    public GithubService(PortfolioProperties properties) {
        this.properties = properties;
        // RestClient.builder() rather than an injected RestClient.Builder: Boot 4 only
        // auto-configures that bean when an HTTP client starter is present, and this is the one
        // outbound call in the app.
        RestClient.Builder configured = RestClient.builder()
                .baseUrl(API_ROOT)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");
        String token = properties.github().token();
        if (token != null && !token.isBlank()) {
            configured = configured.defaultHeader("Authorization", "Bearer " + token);
        }
        this.restClient = configured.build();
    }

    @Cacheable(CacheConfig.GITHUB_CACHE)
    public List<GithubRepoResponse> repos() {
        try {
            JsonNode body = restClient.get()
                    .uri("/users/{user}/repos?per_page=100&sort=pushed", properties.github().username())
                    .retrieve()
                    .body(JsonNode.class);

            List<GithubRepoResponse> repos = new ArrayList<>();
            if (body != null) {
                for (JsonNode node : body) {
                    if (isHidden(node)) {
                        continue;
                    }
                    repos.add(toRepo(node));
                }
            }
            lastGoodRepos.set(List.copyOf(repos));
            return repos;
        } catch (Exception e) {
            List<GithubRepoResponse> stale = lastGoodRepos.get();
            log.warn("GitHub repo fetch failed, serving {} cached repos instead", stale.size(), e);
            return stale;
        }
    }

    public GithubStatsResponse stats() {
        List<GithubRepoResponse> repos = repos();

        Map<String, Integer> languages = new LinkedHashMap<>();
        int stars = 0;
        for (GithubRepoResponse repo : repos) {
            stars += repo.stars();
            if (repo.language() != null) {
                languages.merge(repo.language(), 1, Integer::sum);
            }
        }

        Map<String, Integer> sortedLanguages = new LinkedHashMap<>();
        languages.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> sortedLanguages.put(e.getKey(), e.getValue()));

        List<GithubRepoResponse> topRepos = repos.stream()
                .sorted(Comparator.comparing(GithubRepoResponse::pushedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .toList();

        return new GithubStatsResponse(repos.size(), stars, sortedLanguages, topRepos);
    }

    /** Forks, archived repos and anything on the exclude list are noise on a portfolio. */
    private boolean isHidden(JsonNode node) {
        if (node.path("fork").asBoolean(false) || node.path("archived").asBoolean(false)) {
            return true;
        }
        return properties.github().exclude().contains(node.path("name").asText());
    }

    private GithubRepoResponse toRepo(JsonNode node) {
        List<String> topics = new ArrayList<>();
        node.path("topics").forEach(t -> topics.add(t.asText()));

        String pushedAt = node.path("pushed_at").asText(null);
        return new GithubRepoResponse(
                node.path("name").asText(),
                node.path("description").isNull() ? null : node.path("description").asText(),
                node.path("language").isNull() ? null : node.path("language").asText(),
                topics,
                node.path("stargazers_count").asInt(),
                node.path("forks_count").asInt(),
                node.path("html_url").asText(),
                emptyToNull(node.path("homepage").asText(null)),
                pushedAt == null ? null : Instant.parse(pushedAt));
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
