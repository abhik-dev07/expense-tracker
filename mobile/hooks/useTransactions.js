import { useCallback, useState } from "react";
import { Alert } from "react-native";
import { API_URL } from "../constants/api";

export const useTransactions = (userId) => {
  const [transactions, setTransactions] = useState([]);
  const [summary, setSummary] = useState({
    balance: 0,
    income: 0,
    expenses: 0,
  });
  const [isLoading, setIsLoading] = useState(true);

  // useCallback is used for performance reasons, it will memoize the function
  const fetchTransactions = useCallback(async () => {
    try {
      const response = await fetch(`${API_URL}/transactions/${userId}`);
      if (response.status === 429) {
        throw new Error("Too many requests");
      }
      const data = await response.json();
      setTransactions(data);
    } catch (error) {
      if (error.message !== "Too many requests") {
        console.error("Error fetching transactions:", error);
      }
      throw error;
    }
  }, [userId]);

  const fetchSummary = useCallback(async () => {
    try {
      const response = await fetch(`${API_URL}/transactions/summary/${userId}`);
      if (response.status === 429) {
        throw new Error("Too many requests");
      }
      const data = await response.json();
      setSummary(data);
    } catch (error) {
      if (error.message !== "Too many requests") {
        console.error("Error fetching summary:", error);
      }
      throw error;
    }
  }, [userId]);

  const loadData = useCallback(async (isRefresh = false) => {
    if (!userId) return;

    if (!isRefresh) {
      setIsLoading(true);
    }

    try {
      // can be run in parallel
      await Promise.all([fetchTransactions(), fetchSummary()]);
    } catch (error) {
      if (error.message === "Too many requests") {
        Alert.alert("Too many requests", "Please try again later");
      } else {
        console.error("Error loading data:", error);
      }
    } finally {
      if (!isRefresh) {
        setIsLoading(false);
      }
    }
  }, [fetchTransactions, fetchSummary, userId]);

  const deleteTransaction = async (id) => {
    try {
      const response = await fetch(`${API_URL}/transactions/${id}`, {
        method: "DELETE",
      });
      if (!response.ok) {
        if (response.status === 429) {
          throw new Error("Too many requests");
        }
        throw new Error("Failed to delete transaction");
      }

      // Refresh data after deletion
      loadData();
      Alert.alert("Success", "Transaction deleted successfully");
    } catch (error) {
      if (error.message !== "Too many requests") {
        console.error("Error deleting transaction:", error);
        Alert.alert("Error", error.message);
      } else {
        Alert.alert("Too many requests", "Please try again later");
      }
    }
  };

  return { transactions, summary, isLoading, loadData, deleteTransaction };
};
