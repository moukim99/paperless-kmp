# Phase 10 auth hardening

The Phase 9 refresh flow must be made race-safe before production.

## Required change

Do not perform `SELECT -> batch(update + insert)` for refresh rotation. Two concurrent refresh requests can both observe an unrevoked token.

Use a conditional update first:

```sql
UPDATE refresh_tokens
SET revoked_at=?, replaced_by=?
WHERE id=? AND revoked_at IS NULL AND expires_at>?
```

Only when `meta.changes === 1` should the replacement token be inserted. If `changes === 0`, return `401 invalid_refresh_token`.

This makes refresh-token rotation single-use under concurrent requests.
