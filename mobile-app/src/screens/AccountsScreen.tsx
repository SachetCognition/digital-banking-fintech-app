import React, { useState, useEffect, useCallback } from 'react';
import { View, FlatList, StyleSheet, RefreshControl } from 'react-native';
import { Card, Text, FAB, Chip } from 'react-native-paper';
import { useNavigation } from '@react-navigation/native';
import { api } from '../services/api';

interface Account {
  id: string;
  type: string;
  accountNumber: string;
  balance: number;
  currency: string;
  status: string;
}

export const AccountsScreen: React.FC = () => {
  const navigation = useNavigation();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [refreshing, setRefreshing] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchAccounts = useCallback(async () => {
    try {
      const response = await api.get('/api/v1/accounts');
      setAccounts(response.data);
    } catch (error) {
      console.error('Failed to fetch accounts:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchAccounts(); }, [fetchAccounts]);

  const onRefresh = () => {
    setRefreshing(true);
    fetchAccounts();
  };

  const maskAccountNumber = (num: string) => {
    if (num.length <= 4) return num;
    return '****' + num.slice(-4);
  };

  const renderAccount = ({ item }: { item: Account }) => (
    <Card style={styles.card} onPress={() => navigation.navigate('AccountDetail' as never, { accountId: item.id } as never)}>
      <Card.Content>
        <View style={styles.row}>
          <Text variant="titleMedium">{item.type}</Text>
          <Chip compact>{item.status}</Chip>
        </View>
        <Text variant="bodyMedium" style={styles.accountNumber}>{maskAccountNumber(item.accountNumber)}</Text>
        <Text variant="headlineSmall" style={styles.balance}>
          {item.currency} {item.balance.toLocaleString(undefined, { minimumFractionDigits: 2 })}
        </Text>
      </Card.Content>
    </Card>
  );

  return (
    <View style={styles.container}>
      <FlatList
        data={accounts}
        keyExtractor={(item) => item.id}
        renderItem={renderAccount}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
        ListEmptyComponent={<Text style={styles.empty}>{loading ? 'Loading...' : 'No accounts found'}</Text>}
      />
      <FAB icon="plus" style={styles.fab} onPress={() => navigation.navigate('OpenAccount' as never)} label="New Account" />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  card: { margin: 8, marginHorizontal: 16 },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  accountNumber: { color: '#666', marginTop: 4 },
  balance: { marginTop: 8, fontWeight: 'bold' },
  fab: { position: 'absolute', right: 16, bottom: 16 },
  empty: { textAlign: 'center', marginTop: 48, color: '#999' },
});

export default AccountsScreen;
