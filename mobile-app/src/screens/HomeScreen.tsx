import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Alert, RefreshControl } from 'react-native';
import { Card, Title, Paragraph, Button, FAB, Chip, Badge } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useNavigation } from '@react-navigation/native';
import { http } from '../api/http';

interface Account {
  id: string;
  accountNumber: string;
  accountType: string;
  balance: number;
  currency: string;
  status: string;
}

interface Transaction {
  id: string;
  amount: number;
  description: string;
  type: string;
  status: string;
  timestamp: string;
}

interface Card {
  id: string;
  cardNumber: string;
  cardType: string;
  status: string;
  spendingLimit: number;
  availableBalance: number;
}

const HomeScreen: React.FC = () => {
  const navigation = useNavigation();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [cards, setCards] = useState<Card[]>([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [user, setUser] = useState<any>(null);

  useEffect(() => {
    loadUserData();
    loadDashboardData();
  }, []);

  const loadUserData = async () => {
    try {
      const userData = await AsyncStorage.getItem('user');
      if (userData) {
        setUser(JSON.parse(userData));
      }
    } catch (error) {
      console.error('Error loading user data:', error);
    }
  };

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [accountsRes, transactionsRes, cardsRes] = await Promise.all([
        http.get('/api/v1/accounts'),
        http.get('/api/v1/transactions/recent'),
        http.get('/api/v1/cards')
      ]);
      
      setAccounts(accountsRes.data);
      setTransactions(transactionsRes.data);
      setCards(cardsRes.data);
    } catch (error) {
      Alert.alert('Error', 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadDashboardData();
    setRefreshing(false);
  };

  const formatCurrency = (amount: number) => {
    return `AED ${new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount)}`;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
      case 'completed':
        return '#4CAF50';
      case 'pending':
        return '#FF9800';
      case 'failed':
      case 'blocked':
        return '#F44336';
      default:
        return '#9E9E9E';
    }
  };

  const getAccountIcon = (accountType: string) => {
    switch (accountType.toLowerCase()) {
      case 'checking':
        return '💳';
      case 'savings':
        return '🏦';
      case 'credit':
        return '💳';
      default:
        return '🏦';
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        style={styles.scrollView}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
      >
        {/* Header */}
        <View style={styles.header}>
          <View>
            <Text style={styles.greeting}>Marhaba,</Text>
            <Text style={styles.userName}>{user?.firstName || 'Ahmed'}</Text>
          </View>
          <TouchableOpacity
            style={styles.notificationButton}
            onPress={() => navigation.navigate('Notifications' as never)}
          >
            <Badge size={20} style={styles.badge}>
              3
            </Badge>
            <Text style={styles.notificationIcon}>🔔</Text>
          </TouchableOpacity>
        </View>

        {/* Quick Actions */}
        <View style={styles.quickActions}>
          <TouchableOpacity
            style={styles.quickActionButton}
            onPress={() => navigation.navigate('Transfer' as never)}
          >
            <Text style={styles.quickActionIcon}>💸</Text>
            <Text style={styles.quickActionText}>Transfer</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.quickActionButton}
            onPress={() => navigation.navigate('Pay' as never)}
          >
            <Text style={styles.quickActionIcon}>💳</Text>
            <Text style={styles.quickActionText}>Pay</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.quickActionButton}
            onPress={() => navigation.navigate('Deposit' as never)}
          >
            <Text style={styles.quickActionIcon}>📱</Text>
            <Text style={styles.quickActionText}>Deposit</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.quickActionButton}
            onPress={() => navigation.navigate('Cards' as never)}
          >
            <Text style={styles.quickActionIcon}>💳</Text>
            <Text style={styles.quickActionText}>Cards</Text>
          </TouchableOpacity>
        </View>

        {/* Accounts */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Accounts</Text>
            <TouchableOpacity onPress={() => navigation.navigate('Accounts' as never)}>
              <Text style={styles.seeAllText}>See All</Text>
            </TouchableOpacity>
          </View>
          {accounts.map((account) => (
            <Card key={account.id} style={styles.accountCard}>
              <Card.Content>
                <View style={styles.accountHeader}>
                  <View style={styles.accountInfo}>
                    <Text style={styles.accountIcon}>{getAccountIcon(account.accountType)}</Text>
                    <View>
                      <Text style={styles.accountType}>{account.accountType}</Text>
                      <Text style={styles.accountNumber}>****{account.accountNumber.slice(-4)}</Text>
                    </View>
                  </View>
                  <Chip
                    style={[styles.statusChip, { backgroundColor: getStatusColor(account.status) + '20' }]}
                    textStyle={{ color: getStatusColor(account.status) }}
                  >
                    {account.status}
                  </Chip>
                </View>
                <Text style={styles.accountBalance}>
                  {formatCurrency(account.balance)}
                </Text>
              </Card.Content>
            </Card>
          ))}
        </View>

        {/* Recent Transactions */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Recent Transactions</Text>
            <TouchableOpacity onPress={() => navigation.navigate('Transactions' as never)}>
              <Text style={styles.seeAllText}>See All</Text>
            </TouchableOpacity>
          </View>
          {transactions.map((transaction) => (
            <Card key={transaction.id} style={styles.transactionCard}>
              <Card.Content>
                <View style={styles.transactionHeader}>
                  <View style={styles.transactionInfo}>
                    <Text style={styles.transactionDescription}>{transaction.description}</Text>
                    <Text style={styles.transactionDate}>{formatDate(transaction.timestamp)}</Text>
                  </View>
                  <View style={styles.transactionAmount}>
                    <Text style={[
                      styles.transactionAmountText,
                      { color: transaction.type === 'debit' ? '#F44336' : '#4CAF50' }
                    ]}>
                      {transaction.type === 'debit' ? '-' : '+'}{formatCurrency(transaction.amount)}
                    </Text>
                    <Chip
                      style={[styles.statusChip, { backgroundColor: getStatusColor(transaction.status) + '20' }]}
                      textStyle={{ color: getStatusColor(transaction.status) }}
                    >
                      {transaction.status}
                    </Chip>
                  </View>
                </View>
              </Card.Content>
            </Card>
          ))}
        </View>

        {/* Cards */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Cards</Text>
            <TouchableOpacity onPress={() => navigation.navigate('Cards' as never)}>
              <Text style={styles.seeAllText}>See All</Text>
            </TouchableOpacity>
          </View>
          {cards.map((card) => (
            <Card key={card.id} style={styles.cardCard}>
              <Card.Content>
                <View style={styles.cardHeader}>
                  <View style={styles.cardInfo}>
                    <Text style={styles.cardIcon}>💳</Text>
                    <View>
                      <Text style={styles.cardType}>{card.cardType}</Text>
                      <Text style={styles.cardNumber}>****{card.cardNumber.slice(-4)}</Text>
                    </View>
                  </View>
                  <Chip
                    style={[styles.statusChip, { backgroundColor: getStatusColor(card.status) + '20' }]}
                    textStyle={{ color: getStatusColor(card.status) }}
                  >
                    {card.status}
                  </Chip>
                </View>
                <View style={styles.cardBalance}>
                  <Text style={styles.cardBalanceLabel}>Available Balance</Text>
                  <Text style={styles.cardBalanceAmount}>
                    {formatCurrency(card.availableBalance)}
                  </Text>
                </View>
                <View style={styles.cardLimit}>
                  <Text style={styles.cardLimitLabel}>Spending Limit</Text>
                  <Text style={styles.cardLimitAmount}>
                    {formatCurrency(card.spendingLimit)}
                  </Text>
                </View>
              </Card.Content>
            </Card>
          ))}
        </View>
      </ScrollView>

      {/* Floating Action Button */}
      <FAB
        style={styles.fab}
        icon="plus"
        onPress={() => navigation.navigate('QuickActions' as never)}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollView: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#2196F3',
  },
  greeting: {
    fontSize: 16,
    color: '#E3F2FD',
  },
  userName: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
  },
  notificationButton: {
    position: 'relative',
  },
  badge: {
    position: 'absolute',
    top: -5,
    right: -5,
    backgroundColor: '#FF5722',
  },
  notificationIcon: {
    fontSize: 24,
    color: 'white',
  },
  quickActions: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    padding: 20,
    backgroundColor: 'white',
    marginBottom: 10,
  },
  quickActionButton: {
    alignItems: 'center',
    flex: 1,
  },
  quickActionIcon: {
    fontSize: 24,
    marginBottom: 5,
  },
  quickActionText: {
    fontSize: 12,
    color: '#666',
  },
  section: {
    marginBottom: 20,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 10,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  seeAllText: {
    fontSize: 14,
    color: '#2196F3',
  },
  accountCard: {
    marginHorizontal: 20,
    marginBottom: 10,
    elevation: 2,
  },
  accountHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  accountInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  accountIcon: {
    fontSize: 24,
    marginRight: 10,
  },
  accountType: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  accountNumber: {
    fontSize: 14,
    color: '#666',
  },
  accountBalance: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
  },
  statusChip: {
    height: 24,
  },
  transactionCard: {
    marginHorizontal: 20,
    marginBottom: 10,
    elevation: 1,
  },
  transactionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  transactionInfo: {
    flex: 1,
  },
  transactionDescription: {
    fontSize: 16,
    fontWeight: '500',
    color: '#333',
  },
  transactionDate: {
    fontSize: 14,
    color: '#666',
    marginTop: 2,
  },
  transactionAmount: {
    alignItems: 'flex-end',
  },
  transactionAmountText: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  cardCard: {
    marginHorizontal: 20,
    marginBottom: 10,
    elevation: 2,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 15,
  },
  cardInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  cardIcon: {
    fontSize: 24,
    marginRight: 10,
  },
  cardType: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  cardNumber: {
    fontSize: 14,
    color: '#666',
  },
  cardBalance: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 10,
  },
  cardBalanceLabel: {
    fontSize: 14,
    color: '#666',
  },
  cardBalanceAmount: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#4CAF50',
  },
  cardLimit: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  cardLimitLabel: {
    fontSize: 14,
    color: '#666',
  },
  cardLimitAmount: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  fab: {
    position: 'absolute',
    margin: 16,
    right: 0,
    bottom: 0,
    backgroundColor: '#2196F3',
  },
});

export default HomeScreen;