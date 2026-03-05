import { Slot } from "expo-router";
import SafeScreen from "@/components/SafeScreen";
import { ConvexReactClient } from "convex/react";
import { ConvexAuthProvider } from "@convex-dev/auth/react";
import { StatusBar } from "expo-status-bar";
import { ThemeProvider } from "../context/ThemeContext";
import { KeyboardProvider } from "react-native-keyboard-controller";
import * as SecureStore from "expo-secure-store";

const convex = new ConvexReactClient(
  process.env.EXPO_PUBLIC_CONVEX_URL,
  { unsavedChangesWarning: false }
);

const secureStorage = {
  getItem: (key) => SecureStore.getItemAsync(key),
  setItem: (key, value) => SecureStore.setItemAsync(key, value),
  removeItem: (key) => SecureStore.deleteItemAsync(key),
};

export default function RootLayout() {
  return (
    <ConvexAuthProvider client={convex} storage={secureStorage}>
      <ThemeProvider>
        <SafeScreen>
          <KeyboardProvider>
            <Slot />
          </KeyboardProvider>
        </SafeScreen>
      </ThemeProvider>
      <StatusBar style="dark" />
    </ConvexAuthProvider>
  );
}
