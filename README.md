# Paperless KMP — Release Candidate

Unified source tree assembled from the cumulative Phase 8 project, Phase 9 authentication, and Phase 10 production hardening.

## Included
- Kotlin Multiplatform Android/Desktop
- Room/SQLite local database
- OCR, document capture/scanner, local FTS5
- Correspondents, tags, document types, custom fields
- Cloudflare Workers + D1 + R2
- JWT access tokens and rotating refresh tokens
- Login rate limiting and security headers
- Cloudflare/Vitest test foundation
- GitHub Actions workflow

## Before production
1. Replace `database_id` in `cloudflare/wrangler.jsonc`.
2. Configure the R2 bucket name if needed.
3. Set `JWT_SECRET` with `wrangler secret put JWT_SECRET`.
4. Review and apply D1 migrations.
5. Run Cloudflare tests and Android/Desktop Gradle builds in an environment with the required toolchains.

This archive is a Release Candidate source tree; production deployment has not been performed by this build step.
