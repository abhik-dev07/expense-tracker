import { homeStyles } from "@/assets/styles/home.styles";
import { useTheme } from "@/context/ThemeContext";
import { useAuthActions } from "@convex-dev/auth/react";
import Ionicons from "@expo/vector-icons/Ionicons";
import { Alert, TouchableOpacity } from "react-native";

export const SignOutButton = () => {
  const { signOut } = useAuthActions();
  const { COLORS } = useTheme();
  const styles = homeStyles(COLORS);
  const handleSignOut = async () => {
    Alert.alert("Logout", "Are you sure you want to logout?", [
      { text: "Cancel", style: "cancel" },
      { text: "Logout", style: "destructive", onPress: () => void signOut() },
    ]);
  };
  return (
    <TouchableOpacity style={styles.logoutButton} onPress={handleSignOut}>
      <Ionicons name="log-out-outline" size={22} color={COLORS.text} />
    </TouchableOpacity>
  );
};
