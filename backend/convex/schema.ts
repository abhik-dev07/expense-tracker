import { defineSchema, defineTable } from "convex/server";
import { authTables } from "@convex-dev/auth/server";
import { v } from "convex/values";

export default defineSchema({
    ...authTables,
    users: defineTable({
        name: v.optional(v.string()),
        image: v.optional(v.string()),
        email: v.optional(v.string()),
        emailVerificationTime: v.optional(v.number()),
        phone: v.optional(v.string()),
        phoneVerificationTime: v.optional(v.number()),
        isAnonymous: v.optional(v.boolean()),
        // Added fields:
        username: v.optional(v.string()),
    }).index("email", ["email"]).index("phone", ["phone"]),

    transactions: defineTable({
        userId: v.id("users"),
        title: v.string(),
        amount: v.float64(),
        category: v.string(),
        createdAt: v.string(), // ISO date string, e.g. "2025-05-20"
    })
        .index("by_userId", ["userId"]),
});
