const WINDOW_SECONDS = 15 * 60;
const MAX_ATTEMPTS = 8;

export async function allowLoginAttempt(env: { DB: D1Database }, key: string, now = Math.floor(Date.now() / 1000)): Promise<boolean> {
  const row = await env.DB.prepare(`SELECT attempts, window_start FROM auth_rate_limits WHERE key=? LIMIT 1`).bind(key).first<{attempts:number;window_start:number}>();
  if (!row || now - row.window_start >= WINDOW_SECONDS) {
    await env.DB.prepare(`INSERT INTO auth_rate_limits(key,attempts,window_start) VALUES(?,?,?) ON CONFLICT(key) DO UPDATE SET attempts=1,window_start=excluded.window_start`).bind(key,1,now).run();
    return true;
  }
  if (row.attempts >= MAX_ATTEMPTS) return false;
  await env.DB.prepare(`UPDATE auth_rate_limits SET attempts=attempts+1 WHERE key=?`).bind(key).run();
  return true;
}
