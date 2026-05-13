import React, { useState, useEffect, useCallback } from 'react';
import { View, FlatList, StyleSheet, RefreshControl } from 'react-native';
import { Card, Text, Button, Switch, Chip } from 'react-native-paper';
import { api } from '../services/api';

interface VirtualCard {
  id: string;
  cardType: string;
  maskedNumber: string;
  status: string;
  spendingLimit: number;
  currency: string;
  controls: {
    frozen: boolean;
    atmBlocked: boolean;
    onlineBlocked: boolean;
    internationalBlocked: boolean;
  };
}

export const CardsScreen: React.FC = () => {
  const [cards, setCards] = useState<VirtualCard[]>([]);
  const [refreshing, setRefreshing] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchCards = useCallback(async () => {
    try {
      const response = await api.get('/api/v1/cards');
      setCards(response.data);
    } catch (error) {
      console.error('Failed to fetch cards:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchCards(); }, [fetchCards]);

  const toggleControl = async (cardId: string, control: string, value: boolean) => {
    try {
      await api.put(`/api/v1/cards/${cardId}/controls`, { [control]: value });
      fetchCards();
    } catch (error) {
      console.error('Failed to update card control:', error);
    }
  };

  const requestNewCard = async () => {
    try {
      await api.post('/api/v1/cards', { cardType: 'VIRTUAL_DEBIT' });
      fetchCards();
    } catch (error) {
      console.error('Failed to request card:', error);
    }
  };

  const renderCard = ({ item }: { item: VirtualCard }) => (
    <Card style={styles.card}>
      <Card.Content>
        <View style={styles.row}>
          <Text variant="titleMedium">{item.cardType}</Text>
          <Chip compact mode={item.status === 'ACTIVE' ? 'flat' : 'outlined'}>{item.status}</Chip>
        </View>
        <Text variant="headlineSmall" style={styles.cardNumber}>{item.maskedNumber}</Text>
        <Text variant="bodySmall">Limit: {item.currency} {item.spendingLimit.toLocaleString()}</Text>
        <View style={styles.controls}>
          <View style={styles.controlRow}>
            <Text>Freeze Card</Text>
            <Switch value={item.controls.frozen} onValueChange={(v) => toggleControl(item.id, 'frozen', v)} />
          </View>
          <View style={styles.controlRow}>
            <Text>Block ATM</Text>
            <Switch value={item.controls.atmBlocked} onValueChange={(v) => toggleControl(item.id, 'atmBlocked', v)} />
          </View>
          <View style={styles.controlRow}>
            <Text>Block Online</Text>
            <Switch value={item.controls.onlineBlocked} onValueChange={(v) => toggleControl(item.id, 'onlineBlocked', v)} />
          </View>
          <View style={styles.controlRow}>
            <Text>Block International</Text>
            <Switch value={item.controls.internationalBlocked} onValueChange={(v) => toggleControl(item.id, 'internationalBlocked', v)} />
          </View>
        </View>
      </Card.Content>
    </Card>
  );

  return (
    <View style={styles.container}>
      <FlatList
        data={cards}
        keyExtractor={(item) => item.id}
        renderItem={renderCard}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); fetchCards(); }} />}
        ListEmptyComponent={<Text style={styles.empty}>{loading ? 'Loading...' : 'No cards found'}</Text>}
      />
      <Button mode="contained" onPress={requestNewCard} style={styles.newCardButton}>Request New Card</Button>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  card: { margin: 8, marginHorizontal: 16 },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  cardNumber: { marginVertical: 12, letterSpacing: 2 },
  controls: { marginTop: 12 },
  controlRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 4 },
  empty: { textAlign: 'center', marginTop: 48, color: '#999' },
  newCardButton: { margin: 16 },
});

export default CardsScreen;
