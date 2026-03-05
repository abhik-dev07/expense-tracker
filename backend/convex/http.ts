import { httpRouter } from "convex/server";
import { auth } from "./auth";

const http = httpRouter();

// Register Convex Auth HTTP routes (handles /api/auth/*)
auth.addHttpRoutes(http);

export default http;
