import React, { useState, useEffect } from 'react';
import { View, ScrollView, StyleSheet, Alert } from 'react-native';
import { TextInput, Button, Text, Menu, Divider, List } from 'react-native-paper';
import { api } from '../services/api';

interface Account {
  id: string;
  accountNumber: string;
  balance: number;
  currency: string;
}

interface Transfer {
  id: string;
  amount: number;
  currency: string;
  description: string;
  status: string;
  createdAt: string;
}

export const TransfersScreen: React.FC = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [fromAccountId, setFromAccountId] = useState('');
  const [toAccountId, setToAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [recentTransfers, setRecentTransfers] = useState<Transfer[]>([]);
  const [showFromMenu, setShowFromMenu] = useState(false);
  const [showToMenu, setShowToMenu] = useState(false);

  useEffect(() => {
    fetchAccounts();
    fetchRecentTransfers();
  }, []);

  const fetchAccounts = async () => {
    try {
      const response = await api.get('/api/v1/accounts');
      setAccounts(response.data);
    } catch (error) {
      console.error('Failed to fetch accounts:', error);
    }
  };

  const fetchRecentTransfers = async () => {
    try {
      const response = await api.get('/api/v1/transfers?limit=10');
      setRecentTransfers(response.data);
    } catch (error) {
      console.error('Failed to fetch transfers:', error);
    }
  };

  const handleTransfer = async () => {
    if (!fromAccountId || !toAccountId || !amount) {
      Alert.alert('Error', 'Please fill in all required fields');
      return;
    }
    const numericAmount = parseFloat(amount);
    if (isNaN(numericAmount) || numericAmount <= 0) {
      Alert.alert('Error', 'Please enter a valid amount');
      return;
    }
    setLoading(true);
    try {
      await api.post('/api/v1/transfers', {
        payerAccountId: fromAccountId,
        payeeAccountId: toAccountId,
        amount: numericAmount,
        currency: 'USD',
        description,
      });
      Alert.alert('Success', 'Transfer initiated successfully');
      setAmount('');
      setDescription('');
      fetchRecentTransfers();
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.message || 'Transfer failed');
    } finally {
      setLoading(false);
    }
  };

  const selectedFrom = accounts.find(a => a.id === fromAccountId);
  const selectedTo = accounts.find(a => a.id === toAccountId);

  return (
    <ScrollView style={styles.container}>
      <Text variant="headlineMedium" style={styles.title}>Transfer Funds</Text>

      <Menu visible={showFromMenu} onDismiss={() => setShowFromMenu(false)} anchor={
        <Button mode="outlined" onPress={() => setShowFromMenu(true)} style={styles.input}>
          {selectedFrom ? `****${selectedFrom.accountNumber.slice(-4)} (${selectedFrom.currency} ${selectedFrom.balance})` : 'Select From Account'}
        </Button>
      }>
        {accounts.map(acc => (
          <Menu.Item key={acc.id} title={`****${acc.accountNumber.slice(-4)} - ${acc.currency} ${acc.balance}`}
            onPress={() => { setFromAccountId(acc.id); setShowFromMenu(false); }} />
        ))}
      </Menu>

      <Menu visible={showToMenu} onDismiss={() => setShowToMenu(false)} anchor={
        <Button mode="outlined" onPress={() => setShowToMenu(true)} style={styles.input}>
          {selectedTo ? `****${selectedTo.accountNumber.slice(-4)}` : 'Select To Account'}
        </Button>
      }>
        {accounts.filter(a => a.id !== fromAccountId).map(acc => (
          <Menu.Item key={acc.id} title={`****${acc.accountNumber.slice(-4)}`}
            onPress={() => { setToAccountId(acc.id); setShowToMenu(false); }} />
        ))}
      </Menu>

      <TextInput label="Amount" value={amount} onChangeText={setAmount} mode="outlined" style={styles.input} keyboardType="decimal-pad" />
      <TextInput label="Description (optional)" value={description} onChangeText={setDescription} mode="outlined" style={styles.input} />
      <Button mode="contained" onPress={handleTransfer} loading={loading} disabled={loading} style={styles.button}>
        Send Transfer
      </Button>

      <Divider style={styles.divider} />
      <Text variant="titleMedium" style={styles.subtitle}>Recent Transfers</Text>
      {recentTransfers.map(t => (
        <List.Item key={t.id} title={`${t.currency} ${t.amount}`} description={t.description || t.status}
          right={() => <Text style={styles.status}>{t.status}</Text>} />
      ))}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, backgroundColor: '#fff' },
  title: { textAlign: 'center', marginVertical: 16 },
  subtitle: { marginVertical: 8 },
  input: { marginBottom: 12 },
  button: { marginTop: 8, paddingVertical: 4 },
  divider: { marginVertical: 24 },
  status: { alignSelf: 'center', color: '#666' },
});

export default TransfersScreen;
