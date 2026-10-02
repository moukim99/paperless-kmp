# Cloudflare deployment notes — Phase 8

The mobile/desktop clients call the Worker only; they never receive D1 or R2 credentials.

Set the Worker secret before deployment:

```bash
npx wrangler secret put API_TOKEN
npx wrangler d1 migrations apply paperless --remote
npx wrangler deploy
```

The client sends `Authorization: Bearer <token>`. For production, replace the shared token with an identity-aware JWT/Access integration when user accounts are introduced.

D1/R2 remain bound privately to the Worker. Catalog metadata (correspondent, document type, tags) is synchronized as part of document sync.
