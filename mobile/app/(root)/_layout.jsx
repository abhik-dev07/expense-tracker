import { Redirect, Stack } from "expo-router";
import { Authenticated, Unauthenticated, AuthLoading } from "convex/react";
import PageLoader from "@/components/PageLoader";

export default function Layout() {
  return (
    <>
      <AuthLoading>
        <PageLoader />
      </AuthLoading>
      <Unauthenticated>
        <Redirect href="/sign-in" />
      </Unauthenticated>
      <Authenticated>
        <Stack screenOptions={{ headerShown: false }} />
      </Authenticated>
    </>
  );
}
