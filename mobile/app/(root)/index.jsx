import { useUser } from "@clerk/clerk-expo";
import { useRouter } from "expo-router";
import {
  Alert,
  FlatList,
  Image,
  RefreshControl,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import { SignOutButton } from "@/components/SignOutButton";
import { useTransactions } from "../../hooks/useTransactions";
import { useEffect, useRef, useState } from "react";
import PageLoader from "../../components/PageLoader";
import { homeStyles } from "../../assets/styles/home.styles";
import { Ionicons } from "@expo/vector-icons";
import { BalanceCard } from "../../components/BalanceCard";
import { TransactionItem } from "../../components/TransactionItem";
import NoTransactionsFound from "../../components/NoTransactionsFound";
import { useTheme } from "@/context/ThemeContext";
import ThemeToggler from "../../components/ThemeToggler";
import ActionSheet from "react-native-actions-sheet";

export default function Home() {
  const { user } = useUser();
  const router = useRouter();
  const { COLORS } = useTheme();
  const styles = homeStyles(COLORS);
  const actionSheetRef = useRef(null);
  const [refreshing, setRefreshing] = useState(false);

  const { transactions, summary, isLoading, loadData, deleteTransaction } =
    useTransactions(user.id);

  const onRefresh = async () => {
    setRefreshing(true);
    await loadData();
    setRefreshing(false);
  };

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleDelete = (id) => {
    Alert.alert(
      "Delete Transaction",
      "Are you sure you want to delete this transaction?",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Delete",
          style: "destructive",
          onPress: () => deleteTransaction(id),
        },
      ]
    );
  };

  if (isLoading && !refreshing) return <PageLoader />;

  return (
    <View style={styles.container}>
      <View style={styles.content}>
        {/* HEADER */}
        <View style={styles.header}>
          {/* LEFT */}
          <View style={styles.headerLeft}>
            <Image
              source={require("../../assets/images/logo.png")}
              style={styles.headerLogo}
              resizeMode="contain"
            />
            <View style={styles.welcomeContainer}>
              <Text style={styles.welcomeText}>Welcome,</Text>
              <Text style={styles.usernameText}>
                {user?.username.charAt(0).toUpperCase() +
                  user?.username.slice(1)}
              </Text>
            </View>
          </View>

          {/* RIGHT */}
          <View style={styles.headerRight}>
            <TouchableOpacity
              style={styles.addButton}
              onPress={() => router.push("/create")}
            >
              <Ionicons name="add" size={20} color={COLORS.white} />
              <Text style={styles.addButtonText}>Add</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.settingButton}
              onPress={() => actionSheetRef.current.show()}
            >
              <Ionicons name="settings-outline" size={22} color={COLORS.text} />
            </TouchableOpacity>
            {/* Optional sign out */}
            <SignOutButton />
          </View>
        </View>

        {/* BALANCE CARD */}
        <BalanceCard summary={summary} />

        {/* TRANSACTIONS HEADER */}
        <View style={styles.transactionsHeaderContainer}>
          <Text style={styles.sectionTitle}>Recent Transactions</Text>
        </View>
      </View>

      {/* TRANSACTIONS LIST */}
      <FlatList
        style={styles.transactionsList}
        contentContainerStyle={styles.transactionsListContent}
        data={transactions}
        renderItem={({ item }) => (
          <TransactionItem item={item} onDelete={handleDelete} />
        )}
        ListEmptyComponent={<NoTransactionsFound />}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor={COLORS.primary}
            colors={[COLORS.primary]}
          />
        }
      />
      <ActionSheet
        ref={actionSheetRef}
        containerStyle={styles.actionSheetContainer}
        indicatorStyle={styles.actionSheetIndicator}
        gestureEnabled={true}
        closeOnPressBack={true}
        closeOnTouchBackdrop={true}
        defaultOverlayOpacity={0.3}
      >
        <ThemeToggler hideActionSheet={() => actionSheetRef.current.hide()} />
      </ActionSheet>
    </View>
  );
}
