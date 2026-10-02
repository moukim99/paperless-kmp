import { describe, expect, it } from "vitest";
import { withSecurityHeaders } from "../src/security";

describe("security headers", () => {
  it("adds baseline browser security headers", () => {
    const response = withSecurityHeaders(new Response("ok"));
    expect(response.headers.get("x-content-type-options")).toBe("nosniff");
    expect(response.headers.get("x-frame-options")).toBe("DENY");
    expect(response.headers.get("strict-transport-security")).toContain("max-age=31536000");
    expect(response.headers.get("content-security-policy")).toContain("frame-ancestors 'none'");
  });
});
