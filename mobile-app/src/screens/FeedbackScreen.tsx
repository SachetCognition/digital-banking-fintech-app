import React, { useState } from 'react';
import { View, ScrollView, StyleSheet, Alert } from 'react-native';
import { TextInput, Button, Text, SegmentedButtons, RadioButton } from 'react-native-paper';
import { api } from '../services/api';

const CATEGORIES = ['General', 'Bug', 'Feature Request', 'Complaint'];

export const FeedbackScreen: React.FC = () => {
  const [rating, setRating] = useState(0);
  const [category, setCategory] = useState('General');
  const [feedback, setFeedback] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (rating === 0) {
      Alert.alert('Error', 'Please select a rating');
      return;
    }
    if (!feedback.trim()) {
      Alert.alert('Error', 'Please enter your feedback');
      return;
    }
    setLoading(true);
    try {
      await api.post('/api/v1/feedback', { rating, category, feedback });
      Alert.alert('Thank you!', 'Your feedback has been submitted.');
      setRating(0);
      setFeedback('');
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.message || 'Failed to submit feedback');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text variant="headlineMedium" style={styles.title}>Give Feedback</Text>

      <Text variant="titleMedium" style={styles.label}>Rating</Text>
      <View style={styles.ratingRow}>
        {[1, 2, 3, 4, 5].map(star => (
          <Button key={star} mode={rating >= star ? 'contained' : 'outlined'}
            onPress={() => setRating(star)} style={styles.starButton} compact>
            {'\u2605'}
          </Button>
        ))}
      </View>

      <Text variant="titleMedium" style={styles.label}>Category</Text>
      <RadioButton.Group onValueChange={setCategory} value={category}>
        {CATEGORIES.map(cat => (
          <RadioButton.Item key={cat} label={cat} value={cat} />
        ))}
      </RadioButton.Group>

      <Text variant="titleMedium" style={styles.label}>Your Feedback</Text>
      <TextInput value={feedback} onChangeText={setFeedback} mode="outlined"
        multiline numberOfLines={5} style={styles.input} placeholder="Tell us what you think..." />

      <Button mode="contained" onPress={handleSubmit} loading={loading} disabled={loading} style={styles.button}>
        Submit Feedback
      </Button>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, backgroundColor: '#fff' },
  title: { textAlign: 'center', marginVertical: 16 },
  label: { marginTop: 16, marginBottom: 8 },
  ratingRow: { flexDirection: 'row', justifyContent: 'center', gap: 8 },
  starButton: { minWidth: 44 },
  input: { marginBottom: 16 },
  button: { marginTop: 16, paddingVertical: 4 },
});

export default FeedbackScreen;
