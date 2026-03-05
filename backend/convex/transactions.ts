import { query, mutation } from "./_generated/server";
import { v } from "convex/values";
import { getAuthUserId } from "@convex-dev/auth/server";
import { rateLimiter } from "./rateLimits";

// ─── Get all transactions for the authenticated user ───
export const getByUserId = query({
    args: {},
    handler: async (ctx) => {
        const userId = await getAuthUserId(ctx);
        if (!userId) throw new Error("Not authenticated");

        const transactions = await ctx.db
            .query("transactions")
            .withIndex("by_userId", (q) => q.eq("userId", userId))
            .order("desc")
            .collect();

        // Map to the shape the mobile app expects (snake_case fields)
        return transactions.map((t) => ({
            id: t._id,
            user_id: t.userId,
            title: t.title,
            amount: t.amount,
            category: t.category,
            created_at: t.createdAt,
        }));
    },
});

// ─── Create a new transaction ───
export const create = mutation({
    args: {
        title: v.string(),
        amount: v.float64(),
        category: v.string(),
    },
    handler: async (ctx, args) => {
        const userId = await getAuthUserId(ctx);
        if (!userId) throw new Error("Not authenticated");

        // Rate limit check
        await rateLimiter.limit(ctx, "createTransaction", {
            key: userId,
            throws: true,
        });

        const now = new Date().toISOString().split("T")[0]; // "YYYY-MM-DD"

        const id = await ctx.db.insert("transactions", {
            userId,
            title: args.title,
            amount: args.amount,
            category: args.category,
            createdAt: now,
        });

        const transaction = await ctx.db.get(id);

        // Return in the shape the mobile app expects
        return {
            id: transaction!._id,
            user_id: transaction!.userId,
            title: transaction!.title,
            amount: transaction!.amount,
            category: transaction!.category,
            created_at: transaction!.createdAt,
        };
    },
});

// ─── Delete a transaction by ID ───
export const deleteTransaction = mutation({
    args: { id: v.id("transactions") },
    handler: async (ctx, args) => {
        const userId = await getAuthUserId(ctx);
        if (!userId) throw new Error("Not authenticated");

        // Rate limit check
        await rateLimiter.limit(ctx, "deleteTransaction", {
            key: userId,
            throws: true,
        });

        const existing = await ctx.db.get(args.id);
        if (!existing) {
            throw new Error("Transaction not found");
        }

        // Ensure user owns the transaction
        if (existing.userId !== userId) {
            throw new Error("Unauthorized");
        }

        await ctx.db.delete(args.id);
        return { message: "Transaction deleted successfully" };
    },
});

// ─── Get income/expense/balance summary for the authenticated user ───
export const getSummary = query({
    args: {},
    handler: async (ctx) => {
        const userId = await getAuthUserId(ctx);
        if (!userId) throw new Error("Not authenticated");

        const transactions = await ctx.db
            .query("transactions")
            .withIndex("by_userId", (q) => q.eq("userId", userId))
            .collect();

        let balance = 0;
        let income = 0;
        let expenses = 0;

        for (const t of transactions) {
            balance += t.amount;
            if (t.amount > 0) {
                income += t.amount;
            } else {
                expenses += t.amount;
            }
        }

        return {
            balance: balance.toFixed(2),
            income: income.toFixed(2),
            expenses: expenses.toFixed(2),
        };
    },
});

// ─── Get currently authenticated user info ───
export const currentUser = query({
    args: {},
    handler: async (ctx) => {
        const userId = await getAuthUserId(ctx);
        if (!userId) return null;
        const user = await ctx.db.get(userId);
        return user;
    },
});
