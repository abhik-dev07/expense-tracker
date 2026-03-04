import React, { useRef, useState } from "react";
import { View, Text, TouchableOpacity, StyleSheet, Animated } from "react-native";
import { useTheme } from "../context/ThemeContext";
import { Ionicons } from "@expo/vector-icons";

const ThemeToggler = ({ hideActionSheet }) => {
  const { COLORS, setTheme, themeKey } = useTheme();

  // Local state ensures the toggle updates instantly visually,
  // before we fire the expensive global app re-render.
  const [localTheme, setLocalTheme] = useState(themeKey);
  const isDark = localTheme === "dark";

  // slideAnim represents the state of the toggle (0 for light/coffee, 1 for dark)
  const slideAnim = useRef(new Animated.Value(isDark ? 1 : 0)).current;
  const [containerWidth, setContainerWidth] = useState(0);

  const handleThemeChange = (key) => {
    if (localTheme === key) return;

    // Immediately trigger local UI change
    setLocalTheme(key);

    // 1. Run the fluid sliding animation
    Animated.timing(slideAnim, {
      toValue: key === "dark" ? 1 : 0,
      duration: 300,
      useNativeDriver: true,
    }).start(() => {
      // 2. Hide the action sheet (this also performs its own slide-down animation)
      if (hideActionSheet) {
        hideActionSheet();

        // 3. Wait for the ActionSheet closing animation (approx 300ms) to finish, 
        // THEN update global theme so we don't drop frames.
        setTimeout(() => {
          setTheme(key);
        }, 300);
      } else {
        setTheme(key);
      }
    });
  };

  // The width of the animated highlight pill.
  // 10 comes from padding left and right of 5 on the toggleWrapper.
  const indicatorWidth = Math.max((containerWidth - 10) / 2, 0);

  const translateX = slideAnim.interpolate({
    inputRange: [0, 1],
    outputRange: [0, indicatorWidth],
  });

  // Local derived hardcoded colors so the toggler perfectly anticipates the impending global theme
  const activeIconColor = isDark ? "#FFFFFF" : "#8B593E";
  const inactiveIconColor = isDark ? "#98989D" : "#9A8478";
  const pillColor = isDark ? "#1C1C1E" : "#FFFFFF";

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={[styles.title, { color: COLORS.text }]}>Appearance</Text>
        <Text style={[styles.subtitle, { color: COLORS.textLight }]}>
          Choose your theme
        </Text>
      </View>

      <View
        style={[styles.toggleWrapper, { backgroundColor: COLORS.background }]}
        onLayout={(e) => setContainerWidth(e.nativeEvent.layout.width)}
      >
        {/* Animated Sliding Background */}
        {containerWidth > 0 && (
          <Animated.View
            style={[
              styles.slidingIndicator,
              {
                width: indicatorWidth,
                backgroundColor: pillColor,
                shadowColor: COLORS.shadow,
                transform: [{ translateX }],
              },
            ]}
          />
        )}

        <TouchableOpacity
          style={styles.toggleOption}
          onPress={() => handleThemeChange("light")}
          activeOpacity={1}
        >
          <Ionicons
            name="cafe"
            size={20}
            color={!isDark ? activeIconColor : inactiveIconColor}
            style={styles.icon}
          />
          <Text
            style={[
              styles.toggleText,
              {
                color: !isDark ? activeIconColor : inactiveIconColor,
                fontWeight: !isDark ? "700" : "500",
              },
            ]}
          >
            Coffee
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.toggleOption}
          onPress={() => handleThemeChange("dark")}
          activeOpacity={1}
        >
          <Ionicons
            name="moon"
            size={20}
            color={isDark ? activeIconColor : inactiveIconColor}
            style={styles.icon}
          />
          <Text
            style={[
              styles.toggleText,
              {
                color: isDark ? activeIconColor : inactiveIconColor,
                fontWeight: isDark ? "700" : "500",
              },
            ]}
          >
            Dark Mode
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: "100%",
    paddingHorizontal: 20,
    paddingBottom: 30,
    paddingTop: 10,
  },
  header: {
    alignItems: "center",
    marginBottom: 24,
  },
  title: {
    fontSize: 22,
    fontWeight: "800",
    letterSpacing: 0.5,
    marginBottom: 6,
  },
  subtitle: {
    fontSize: 14,
    opacity: 0.8,
  },
  toggleWrapper: {
    flexDirection: "row",
    height: 60,
    borderRadius: 30,
    padding: 5,
    width: "100%",
    position: "relative",
  },
  slidingIndicator: {
    position: "absolute",
    top: 5,
    bottom: 5,
    left: 5,
    borderRadius: 25,
    elevation: 3,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.12,
    shadowRadius: 6,
  },
  toggleOption: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 1, // Ensures text stays strictly above sliding background
  },
  icon: {
    marginRight: 8,
  },
  toggleText: {
    fontSize: 16,
    letterSpacing: 0.3,
  },
});

export default ThemeToggler;
