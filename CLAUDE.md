# CLAUDE.md

Guidance for Claude Code (claude.ai/code) when working in this repository.

## Commands

```bash
./mvnw clean package
./mvnw spring-boot:run                  # port 8083
./mvnw test
./mvnw test -Dtest=ContactControllerTest
```

Tests run against in-memory H2 with `create-drop`; local runs use a file-backed H2 under `./data`.
Neither needs any setup.

## Spring Boot 4 specifics

This is Boot 4, not Boot 3. Two differences bite immediately:

1. **Jackson 3.** Imports are `tools.jackson.*`, not `com.fasterxml.jackson.*`. The mapper bean is
   `JsonMapper`, not `ObjectMapper`.
2. **`spring-boot-starter-web` no longer pulls in a JSON mapper.** `spring-boot-starter-json` is an
   explicit dependency for that reason. Do not remove it.

There is also no auto-configured `RestClient.Builder` bean without an HTTP client starter, so
`GithubService` calls `RestClient.builder()` directly.

## Architecture

```
config/       PortfolioProperties (all portfolio.* settings), CorsConfig, CacheConfig, ApiKeyFilter
controller/   Contact, Content, Github, Analytics, Admin
service/      ContentService, ContentSeeder, GithubService, TelegramService, AnalyticsService,
              RateLimitService
domain/       Profile, Stat, Project, Experience, SkillGroup, Certification, AnalyticsEvent
repository/   One Spring Data interface per aggregate, each in its own file
dto/          request/ and response/ records
exception/    ApiExceptions + GlobalExceptionHandler
```

Configuration is bound once into the `PortfolioProperties` record. Add new settings there rather
than scattering `@Value` annotations.

There is no Spring Security dependency. `ApiKeyFilter` guards `/api/admin/**` with a constant-time
comparison against `X-API-Key`; everything else is a public read. Keep it that way unless real user
accounts arrive.

## Content model

`src/main/resources/seed/portfolio.json` is the single source of truth for site content.
`ContentSeeder` upserts it by slug on every boot and deletes rows whose slug no longer appears.
To change what the site says, edit that JSON. Do not write one-off SQL or seed through a
controller.

The frontend repository generates `src/data/fallback.ts` from this same file. Changing the seed
without regenerating that file lets the two drift, which is the failure this arrangement exists to
prevent.

Schema management is `ddl-auto=update`, deliberately. See the README for the reasoning and the
condition under which to revisit it.

Note `Stat.value` maps to a `stat_value` column: `value` is reserved in both H2 and Postgres.

## Things not to undo

- **CORS origins carry no trailing slash.** A browser `Origin` header is scheme, host, and port
  only. The previous `@CrossOrigin("https://10krhinooo.github.io/")` never matched and silently
  broke every contact submission. Origins are configured in `PortfolioProperties`, applied globally
  by `CorsConfig`, and there should be no `@CrossOrigin` annotations anywhere.
- **The honeypot returns success.** A filtered submission must be indistinguishable from a real one
  or bots learn to adapt.
- **`GithubService` serves stale data on failure** rather than propagating an error, so the site's
  projects section never goes blank because of an upstream blip.
- **`/api/analytics/event` always returns 202**, including when rate limited. A dropped metric is
  never worth an error in front of a visitor.
- **Analytics stores no raw IPs and no cookies.** Only a salted, truncated hash.

## Adding an endpoint

Return a record from `dto/response/`, not an entity, so the JPA model can change without breaking
the frontend contract. Throw from `ApiExceptions` rather than returning bare status codes, so every
error keeps the shared `ApiError` shape. Add the route to the table in the README.
