import { useAuthActions } from "@convex-dev/auth/react";
import { Link, useRouter } from "expo-router";
import { Text, TextInput, TouchableOpacity, View } from "react-native";
import { useEffect, useState } from "react";
import { Ionicons } from "@expo/vector-icons";
import { Image } from "expo-image";
import { useTheme } from "@/context/ThemeContext";
import { authStyles } from "@/assets/styles/auth.styles";
import { KeyboardAwareScrollView } from "react-native-keyboard-controller";

export default function Page() {
  const { signIn, signOut } = useAuthActions();
  const router = useRouter();
  const { COLORS } = useTheme();
  const styles = authStyles(COLORS);

  const [step, setStep] = useState("signIn"); // "signIn" | "forgot" | "reset-verification"
  const [emailAddress, setEmailAddress] = useState("");
  const [password, setPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(""), 2000);
      return () => clearTimeout(timer);
    }
  }, [error]);

  const onSignInPress = async () => {
    if (!emailAddress || !password) {
      setError("Please fill in all fields before signing in.");
      return;
    }

    setIsLoading(true);
    try {
      await signIn("password", {
        email: emailAddress,
        password,
        flow: "signIn",
      });
      router.replace("/");
    } catch (err) {
      const errorMessage = err?.message || err?.toString() || "";
      if (errorMessage.includes("InvalidAccountId") || errorMessage.includes("not found")) {
        setError("Couldn't find your account. Please check your email.");
      } else if (errorMessage.includes("InvalidSecret") || errorMessage.includes("password")) {
        setError("Password is incorrect. Please try again.");
      } else if (errorMessage.includes("email")) {
        setError("Please enter a valid email address.");
      } else {
        setError("An error occurred. Please try again.");
      }
      console.error("Sign-in error:", err);
    } finally {
      setIsLoading(false);
    }
  };

  const onForgotPasswordPress = async () => {
    if (!emailAddress) {
      setError("Please enter your email first.");
      return;
    }

    setIsLoading(true);
    try {
      await signIn("password", {
        email: emailAddress,
        flow: "reset",
      });
      setStep("reset-verification");
    } catch (err) {
      setError("An error occurred sending the reset code.");
      console.error("Reset error:", err);
    } finally {
      setIsLoading(false);
    }
  };

  const onResetVerifyPress = async () => {
    if (!code || !newPassword) {
      setError("Please enter both fields.");
      return;
    }

    if (newPassword.length < 8) {
      setError("Passwords must be 8 characters or more.");
      return;
    }

    setIsLoading(true);
    try {
      await signIn("password", {
        email: emailAddress,
        code,
        newPassword,
        flow: "reset-verification",
      });
      // Force sign out so they don't automatically get redirected
      await signOut();
      setStep("signIn");
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

  if (step === "forgot") {
    return (
      <View style={styles.container}>
        <KeyboardAwareScrollView bottomOffset={62} showsVerticalScrollIndicator={false}>
          <Text style={styles.title}>Forgot Password</Text>
          <Text style={[styles.footerText, { textAlign: 'center', marginBottom: 20 }]}>
            Enter your email to receive a password reset code.
          </Text>

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
            value={emailAddress}
            placeholder="Enter email"
            placeholderTextColor="#9A8478"
            onChangeText={setEmailAddress}
          />

          <TouchableOpacity
            style={[styles.button, isLoading && styles.buttonDisabled]}
            onPress={onForgotPasswordPress}
          >
            <Text style={styles.buttonText}>
              {isLoading ? "Sending..." : "Send Reset Code"}
            </Text>
          </TouchableOpacity>

          <TouchableOpacity style={{ marginTop: 20, alignItems: 'center' }} onPress={() => setStep("signIn")}>
            <Text style={styles.linkText}>Back to Sign In</Text>
          </TouchableOpacity>
        </KeyboardAwareScrollView>
      </View>
    );
  }

  if (step === "reset-verification") {
    return (
      <View style={styles.verificationContainer}>
        <Text style={styles.verificationTitle}>Reset Password</Text>
        <Text style={[styles.footerText, { textAlign: 'center', marginBottom: 20 }]}>
          Enter the code sent to {emailAddress} and your new password.
        </Text>

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
          style={[styles.verificationInput, error && styles.errorInput, { letterSpacing: 2 }]}
          value={code}
          placeholder="Reset Code"
          placeholderTextColor="#9A8478"
          onChangeText={setCode}
        />

        <TextInput
          style={[styles.input, error && styles.errorInput, { width: '100%', marginBottom: 20 }]}
          value={newPassword}
          placeholder="New Password"
          placeholderTextColor="#9A8478"
          secureTextEntry={true}
          onChangeText={setNewPassword}
        />

        <TouchableOpacity
          onPress={onResetVerifyPress}
          style={[styles.button, isLoading && styles.buttonDisabled, { width: '100%' }]}
        >
          <Text style={styles.buttonText}>
            {isLoading ? "Resetting..." : "Reset Password"}
          </Text>
        </TouchableOpacity>

        <TouchableOpacity style={{ marginTop: 20 }} onPress={() => setStep("signIn")}>
          <Text style={styles.linkText}>Back to Sign In</Text>
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
          source={require("../../assets/images/revenue-i4.png")}
          style={styles.illustration}
        />
        <Text style={styles.title}>Welcome Back</Text>

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
          value={emailAddress}
          placeholder="Enter email"
          placeholderTextColor="#9A8478"
          onChangeText={(emailAddress) => setEmailAddress(emailAddress)}
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
          style={{ alignSelf: 'flex-end', marginBottom: 15 }}
          onPress={() => setStep("forgot")}
        >
          <Text style={styles.linkText}>Forgot Password?</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.button, isLoading && styles.buttonDisabled]}
          onPress={onSignInPress}
        >
          <Text style={styles.buttonText}>
            {isLoading ? "Signing In..." : "Sign In"}
          </Text>
        </TouchableOpacity>

        <View style={styles.footerContainer}>
          <Text style={styles.footerText}>Don&apos;t have an account?</Text>

          <Link href="/sign-up" asChild>
            <TouchableOpacity>
              <Text style={styles.linkText}>Sign up</Text>
            </TouchableOpacity>
          </Link>
        </View>
      </KeyboardAwareScrollView>
    </View>
  );
}
