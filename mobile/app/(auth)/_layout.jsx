import { Redirect, Stack } from "expo-router";
import { Authenticated, Unauthenticated, AuthLoading } from "convex/react";
import PageLoader from "@/components/PageLoader";

export default function AuthRoutesLayout() {
  return (
    <>
      <AuthLoading>
        <PageLoader />
      </AuthLoading>
      <Authenticated>
        <Redirect href={"/"} />
      </Authenticated>
      <Unauthenticated>
        <Stack screenOptions={{ headerShown: false }} />
      </Unauthenticated>
    </>
  );
}
