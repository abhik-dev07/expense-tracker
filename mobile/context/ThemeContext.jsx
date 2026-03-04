import React, { createContext, useContext, useState, useEffect } from "react";
import { LayoutAnimation, Platform, UIManager } from "react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";

// Enable LayoutAnimation for Android
if (Platform.OS === "android") {
  if (UIManager.setLayoutAnimationEnabledExperimental) {
    UIManager.setLayoutAnimationEnabledExperimental(true);
  }
}

const THEMES = {
  light: {
    primary: "#8B593E",
    background: "#FFF8F3",
    text: "#4A3428",
    border: "#E5D3B7",
    white: "#FFFFFF",
    textLight: "#9A8478",
    expense: "#E74C3C",
    income: "#2ECC71",
    card: "#FFFFFF",
    shadow: "#000000",
  },
  dark: {
    primary: "#FFFFFF",
    background: "#000000",
    text: "#F2F2F7",
    border: "#38383A",
    white: "#000000",
    textLight: "#98989D",
    expense: "#FF453A",
    income: "#32D74B",
    card: "#1C1C1E",
    shadow: "#000000",
  },
};

const ThemeContext = createContext();

export const ThemeProvider = ({ children }) => {
  const [themeKey, setThemeKey] = useState("light");

  const setTheme = async (key) => {
    if (THEMES[key]) {
      LayoutAnimation.configureNext(
        LayoutAnimation.create(
          700,
          LayoutAnimation.Types.easeInEaseOut,
          LayoutAnimation.Properties.opacity
        )
      );

      setThemeKey(key);
      await AsyncStorage.setItem("app_theme", key);
    }
  };

  useEffect(() => {
    const loadTheme = async () => {
      const saved = await AsyncStorage.getItem("app_theme");
      if (saved === "dark") {
        setThemeKey("dark");
      } else {
        setThemeKey("light"); // Defaults legacy 'coffee' or other colors to light
      }
    };
    loadTheme();
  }, []);

  const COLORS = THEMES[themeKey] || THEMES.light;

  return (
    <ThemeContext.Provider value={{ COLORS, themeKey, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => useContext(ThemeContext);
