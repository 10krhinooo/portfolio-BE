package com.kimangaVictor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * All tunable portfolio settings in one place, bound from {@code portfolio.*} properties.
 */
@ConfigurationProperties(prefix = "portfolio")
public record PortfolioProperties(
        Cors cors,
        Contact contact,
        Github github,
        Admin admin,
        Analytics analytics,
        Seed seed
) {

    public record Cors(List<String> allowedOrigins) {
    }

    /**
     * @param maxPerHour    submissions allowed per client IP per hour
     * @param maxMessageLen longest accepted message body
     */
    public record Contact(int maxPerHour, int maxMessageLen) {
    }

    /**
     * @param username GitHub account whose public repos are proxied
     * @param token    optional PAT; raises the rate limit from 60/h to 5000/h
     * @param exclude  repo names never surfaced on the site
     */
    public record Github(String username, String token, List<String> exclude) {
    }

    public record Admin(String apiKey) {
    }

    /**
     * @param salt      mixed into visitor hashes so they cannot be reversed to an IP
     * @param maxPerMin analytics events accepted per client IP per minute
     */
    public record Analytics(String salt, int maxPerMin) {
    }

    /**
     * @param reset when true, wipes content tables on boot before re-seeding from the JSON resource
     */
    public record Seed(boolean reset) {
    }
}
