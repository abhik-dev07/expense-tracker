import ratelimit from "../config/upstash.js";

const rateLimiter = async (req, res, next) => {
  try {
    const { success } = await ratelimit.limit("my-rate-limit");

    if (!success) {
      return res
        .status(429)
        .json({ error: "Too many requests, Please try again later." });
    }
    next();
  } catch (error) {
    console.error("Rate limiter error:", error.message);
    // Fail open — allow the request through if rate limiter is unavailable
    next();
  }
};

export default rateLimiter;
