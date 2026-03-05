import { useQuery, useMutation } from "convex/react";
import { api } from "../../backend/convex/_generated/api";
import { Alert } from "react-native";

export const useTransactions = () => {
  // Real-time reactive queries — automatically update when data changes
  const transactions = useQuery(api.transactions.getByUserId) ?? [];
  const summary = useQuery(api.transactions.getSummary) ?? {
    balance: "0.00",
    income: "0.00",
    expenses: "0.00",
  };
  const user = useQuery(api.transactions.currentUser);

  const createTransaction = useMutation(api.transactions.create);
  const deleteTransactionMutation = useMutation(
    api.transactions.deleteTransaction
  );

  // With Convex, data is reactive — no need for manual loading/refreshing
  const isLoading = transactions === undefined || summary === undefined;

  const deleteTransaction = async (id) => {
    try {
      await deleteTransactionMutation({ id });
      Alert.alert("Success", "Transaction deleted successfully");
    } catch (error) {
      console.error("Error deleting transaction:", error);
      if (
        error.message?.includes("rate") ||
        error.message?.includes("RateLimited")
      ) {
        Alert.alert("Too many requests", "Please try again later");
      } else {
        Alert.alert("Error", error.message || "Failed to delete transaction");
      }
    }
  };

  return {
    transactions,
    summary,
    isLoading,
    loadData: () => { },
    deleteTransaction,
    createTransaction,
    user,
  };
};
