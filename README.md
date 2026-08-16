# portfolio-BE

The API behind [10krhinooo.github.io](https://10krhinooo.github.io), Victor Kimanga's portfolio.

Spring Boot 4 on Java 21. It serves the site's content, proxies live GitHub data, delivers contact
form submissions to Telegram, and records privacy-preserving visitor analytics.

## Running locally

```bash
./mvnw spring-boot:run     # http://localhost:8083
./mvnw test
./mvnw clean package
```

No setup is required for a first run. The datasource defaults to a file-backed H2 database in
`./data`, and content is seeded automatically on boot.

## Configuration

Every setting is an environment variable with a working default, except the Telegram credentials.

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8083` | HTTP port |
| `DATABASE_URL` | file H2 in `./data` | JDBC URL. Railway's Postgres plugin supplies this |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | `sa` / empty | Datasource credentials |
| `TELEGRAM_BOT_TOKEN` | empty | Bot token. Contact delivery fails without it |
| `TELEGRAM_CHAT_ID` | empty | Chat that receives submissions |
| `ALLOWED_ORIGINS` | Pages origin plus localhost | Comma separated CORS origins |
| `CONTACT_MAX_PER_HOUR` | `5` | Contact submissions per IP per hour |
| `GITHUB_USERNAME` | `10krhinooo` | Account whose repos are proxied |
| `GITHUB_TOKEN` | empty | Optional PAT. Raises the GitHub limit from 60/h to 5000/h |
| `ADMIN_API_KEY` | empty | Guards `/api/admin/**`. Empty means admin routes always reject |
| `ANALYTICS_SALT` | placeholder | Salt for visitor hashes. **Must be set in production** |
| `ANALYTICS_MAX_PER_MIN` | `60` | Analytics events per IP per minute |
| `SEED_RESET` | `false` | Wipe content tables before reseeding |

Values can also come from a `.env` file, which is loaded on startup and gitignored.

## API

All routes are public reads except `/api/admin/**`.

| Method | Path | Notes |
|---|---|---|
| `GET` | `/api/content` | Profile, stats, projects, experience, skills, certifications in one call |
| `GET` | `/api/profile` | |
| `GET` | `/api/projects` | |
| `GET` | `/api/experience` | |
| `GET` | `/api/skills` | |
| `GET` | `/api/certifications` | |
| `GET` | `/api/github/repos` | Cached one hour, forks and archived repos filtered out |
| `GET` | `/api/github/stats` | Language mix, star total, most recently pushed repos |
| `POST` | `/api/contact` | `{name, email, message, website}` |
| `POST` | `/api/analytics/event` | `{type, path, referrer, target}`, always answers 202 |
| `GET` | `/api/admin/analytics` | Requires `X-API-Key` |
| `GET` | `/actuator/health` | |

`/api/content` exists because the service sleeps on Railway's free tier. Fetching everything in one
request means a visitor pays the cold start once rather than six times.

Errors share one shape:

```json
{ "code": "validation_failed", "message": "Some fields need attention.", "fields": { "email": "..." } }
```

## Content is seeded from JSON

`src/main/resources/seed/portfolio.json` is the single source of truth for site content.
`ContentSeeder` loads it on every boot and upserts by slug, so editing the file and redeploying
updates the API with no manual migration. Rows whose slug disappears from the JSON are deleted, so
the database cannot drift ahead of the file.

The frontend's `src/data/fallback.ts` is generated from this same file. Regenerate it when the seed
changes.

Schema management is Hibernate `ddl-auto=update` rather than a migration tool. The content model is
small and fully rebuildable from the seed, so a migration framework would be weight without
benefit. Revisit that if the data ever stops being derivable from the seed.

## Contact form protection

The endpoint is an unauthenticated relay into a personal Telegram chat, so it is defended in layers:

- Bean validation with per-field messages the frontend renders inline.
- A `website` honeypot. Real users never fill it; submissions that do get a normal `200` and are
  silently discarded, so bots get no signal.
- A per-IP hourly token bucket, keyed on `X-Forwarded-For` because Railway terminates TLS upstream.
- Control characters stripped and long blank runs collapsed before formatting for Telegram.

Rate limit buckets are in memory. That is correct for a single container and would need Redis if
the service is ever scaled out.

## Analytics and privacy

No cookies and no raw IP addresses are stored. Visitors are counted through a salted SHA-256 of the
client IP truncated to 16 hex characters, which supports distinct-visitor counts without being
reversible to a person. Set `ANALYTICS_SALT` in production; the default placeholder is not a secret.

## Deploying

Pushing to `main` runs `.github/workflows/maven-publish.yml`, which builds and deploys to Railway
using the `RAILWAY_TOKEN` secret.
