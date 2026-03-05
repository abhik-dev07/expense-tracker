import { useState, useEffect } from "react";
import { Text, TextInput, TouchableOpacity, View } from "react-native";
import { useAuthActions } from "@convex-dev/auth/react";
import { useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { Image } from "expo-image";
import { useTheme } from "@/context/ThemeContext";
import { authStyles } from "@/assets/styles/auth.styles";
import { KeyboardAwareScrollView } from "react-native-keyboard-controller";

export default function SignUpScreen() {
  const { signIn, signOut } = useAuthActions();
  const router = useRouter();
  const { COLORS } = useTheme();
  const styles = authStyles(COLORS);

  const [step, setStep] = useState("signUp"); // "signUp" | "email-verification"
  const [emailAddress, setEmailAddress] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(""), 2000);
      return () => clearTimeout(timer);
    }
  }, [error]);

  const onSignUpPress = async () => {
    if (!username || !emailAddress || !password) {
      setError("Please fill in all fields before signing up.");
      return;
    }

    if (password.length < 8) {
      setError("Passwords must be 8 characters or more.");
      return;
    }

    setIsLoading(true);
    try {
      await signIn("password", {
        email: emailAddress,
        password,
        name: username,
        username,
        flow: "signUp",
      });
      // Convex Auth with verify option requires a code step
      setStep("email-verification");
    } catch (err) {
      const errorMessage = err?.message || err?.toString() || "";
      if (errorMessage.includes("already exists") || errorMessage.includes("AccountAlreadyExists")) {
        setError("That email address is already in use. Please try another.");
      } else if (errorMessage.includes("email") || errorMessage.includes("invalid")) {
        setError("Please enter a valid email address.");
      } else if (errorMessage.includes("password")) {
        setError("Passwords must be 8 characters or more.");
      } else {
        setError("An error occurred. Please try again.");
      }
      console.error("Sign-up error:", err);
    } finally {
      setIsLoading(false);
    }
  };

  const onVerifyPress = async () => {
    if (!code) {
      setError("Please enter the verification code.");
      return;
    }

    setIsLoading(true);
    try {
      await signIn("password", {
        email: emailAddress,
        code,
        flow: "email-verification",
      });
      // Force sign out so they don't automatically get redirected to the Home screen
      await signOut();
      router.replace("/sign-in");
    } catch (err) {
      const errorMessage = err?.message || err?.toString() || "";
      if (errorMessage.includes("InvalidSecret") || errorMessage.includes("code")) {
        setError("Verification code is incorrect. Please try again.");
      } else {
        setError("An error occurred. Please try again.");
      }
      console.error("Verification error:", err);
    } finally {
      setIsLoading(false);
    }
  };

  if (step === "email-verification") {
    return (
      <View style={styles.verificationContainer}>
        <Text style={styles.verificationTitle}>Verify your email</Text>

        {error ? (
          <View style={styles.errorBox}>
            <Ionicons name="alert-circle" size={20} color={COLORS.expense} />
            <Text style={styles.errorText}>{error}</Text>
            <TouchableOpacity onPress={() => setError("")}>
              <Ionicons name="close" size={20} color={COLORS.textLight} />
            </TouchableOpacity>
          </View>
        ) : null}

        <TextInput
          style={[styles.verificationInput, error && styles.errorInput]}
          value={code}
          placeholder="Enter your verification code"
          placeholderTextColor="#9A8478"
          onChangeText={(code) => setCode(code)}
        />

        <TouchableOpacity
          onPress={onVerifyPress}
          style={[styles.button, isLoading && styles.buttonDisabled]}
        >
          <Text style={styles.buttonText}>
            {isLoading ? "Verifying..." : "Verify"}
          </Text>
        </TouchableOpacity>

        <TouchableOpacity style={{ marginTop: 20 }} onPress={() => setStep("signUp")}>
          <Text style={styles.linkText}>Back to Sign Up</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <KeyboardAwareScrollView
        bottomOffset={62}
        showsVerticalScrollIndicator={false}
      >
        <Image
          source={require("../../assets/images/revenue-i2.png")}
          style={styles.illustration}
        />

        <Text style={styles.title}>Create Account</Text>

        {error ? (
          <View style={styles.errorBox}>
            <Ionicons name="alert-circle" size={20} color={COLORS.expense} />
            <Text style={styles.errorText}>{error}</Text>
            <TouchableOpacity onPress={() => setError("")}>
              <Ionicons name="close" size={20} color={COLORS.textLight} />
            </TouchableOpacity>
          </View>
        ) : null}

        <TextInput
          style={[styles.input, error && styles.errorInput]}
          autoCapitalize="none"
          value={username}
          placeholderTextColor="#9A8478"
          placeholder="Enter username"
          onChangeText={(username) => setUsername(username)}
        />
        <TextInput
          style={[styles.input, error && styles.errorInput]}
          autoCapitalize="none"
          value={emailAddress}
          placeholderTextColor="#9A8478"
          placeholder="Enter email"
          onChangeText={(email) => setEmailAddress(email)}
        />

        <TextInput
          style={[styles.input, error && styles.errorInput]}
          value={password}
          placeholder="Enter password"
          placeholderTextColor="#9A8478"
          secureTextEntry={true}
          onChangeText={(password) => setPassword(password)}
        />

        <TouchableOpacity
          style={[styles.button, isLoading && styles.buttonDisabled]}
          onPress={onSignUpPress}
        >
          <Text style={styles.buttonText}>
            {isLoading ? "Signing Up..." : "Sign Up"}
          </Text>
        </TouchableOpacity>

        <View style={styles.footerContainer}>
          <Text style={styles.footerText}>Already have an account?</Text>
          <TouchableOpacity onPress={() => router.back()}>
            <Text style={styles.linkText}>Sign in</Text>
          </TouchableOpacity>
        </View>
      </KeyboardAwareScrollView>
    </View>
  );
}
