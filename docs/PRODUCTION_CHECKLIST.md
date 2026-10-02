# Production Candidate Checklist

## Secrets
- [ ] `JWT_SECRET` created with a cryptographically random value using Wrangler Secrets.
- [ ] `CLOUDFLARE_API_TOKEN` is stored only in GitHub Actions secrets.
- [ ] No API token, JWT secret, D1 ID secret, or refresh token is committed.

## Cloudflare
- [ ] Replace `REPLACE_WITH_D1_DATABASE_ID` in the deployment environment.
- [ ] Create the R2 bucket `paperless-documents`.
- [ ] Apply D1 migrations remotely before deployment.
- [ ] Configure the production Worker route/domain.
- [ ] Verify CORS only allows the application origins.

## Authentication
- [ ] Apply the race-safe refresh rotation described in `cloudflare/src/auth-patch-notes.md`.
- [ ] Add login rate limiting using `auth_rate_limits`.
- [ ] Return generic login errors to avoid account enumeration.
- [ ] Revoke all active refresh tokens when an account is disabled or compromised.

## Data
- [ ] Test offline capture and replay after network recovery.
- [ ] Test duplicate detection with identical files.
- [ ] Test 409 conflict handling.
- [ ] Test R2 upload failure and retry.
- [ ] Test D1 migration rollback/backup procedure.

## Release
- [ ] `npm test` passes.
- [ ] Android Gradle build passes on a machine with Android SDK.
- [ ] Desktop Gradle build passes.
- [ ] Release APK/package is signed using a secret-managed signing key.
- [ ] Crash/error monitoring is configured.
