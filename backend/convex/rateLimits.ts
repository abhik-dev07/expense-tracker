import { RateLimiter, MINUTE, HOUR } from "@convex-dev/rate-limiter";
import { components } from "./_generated/api";

export const rateLimiter = new RateLimiter(components.rateLimiter, {
    // Per-user: fetch transactions (generous for reads)
    fetchTransactions: {
        kind: "token bucket",
        rate: 30,
        period: MINUTE,
        capacity: 10,
    },

    // Per-user: create transaction
    createTransaction: {
        kind: "token bucket",
        rate: 10,
        period: MINUTE,
        capacity: 5,
    },

    // Per-user: delete transaction
    deleteTransaction: {
        kind: "token bucket",
        rate: 10,
        period: MINUTE,
        capacity: 5,
    },

    // Per-user: fetch summary
    fetchSummary: {
        kind: "token bucket",
        rate: 30,
        period: MINUTE,
        capacity: 10,
    },

    // Global: sign-up rate limit to prevent abuse
    signUp: {
        kind: "fixed window",
        rate: 100,
        period: HOUR,
    },

    // Per-user: failed login attempts
    failedLogin: {
        kind: "token bucket",
        rate: 5,
        period: MINUTE,
        capacity: 5,
    },
});
