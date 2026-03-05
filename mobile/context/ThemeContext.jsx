import React, { createContext, useContext } from "react";
import { useColorScheme } from "react-native";

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
    primary: "#8B593E",
    background: "#000000",
    text: "#F2F2F7",
    border: "#38383A",
    white: "#FFFFFF",
    textLight: "#98989D",
    expense: "#FF453A",
    income: "#32D74B",
    card: "#1C1C1E",
    shadow: "#000000",
  },
};

const ThemeContext = createContext();

export const ThemeProvider = ({ children }) => {
  const colorScheme = useColorScheme();
  const themeKey = colorScheme === "dark" ? "dark" : "light";
  const COLORS = THEMES[themeKey] || THEMES.light;

  return (
    <ThemeContext.Provider value={{ COLORS, themeKey }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => useContext(ThemeContext);
