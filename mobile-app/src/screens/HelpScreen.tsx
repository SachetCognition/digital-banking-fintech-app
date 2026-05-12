import React, { useState } from 'react';
import { View, ScrollView, StyleSheet } from 'react-native';
import { Searchbar, List, Button, Text, Divider } from 'react-native-paper';

const FAQ_ITEMS = [
  { question: 'How do I reset my password?', answer: 'Go to Profile > Change Password, or use the "Forgot Password" link on the login screen.' },
  { question: 'How do I enable two-factor authentication?', answer: 'Navigate to Profile and toggle the Two-Factor Authentication switch.' },
  { question: 'How do I open a new account?', answer: 'Go to the Accounts tab and tap the "New Account" button at the bottom.' },
  { question: 'How do I freeze my card?', answer: 'Go to Cards tab, find your card, and toggle the "Freeze Card" switch.' },
  { question: 'What are the transfer limits?', answer: 'Daily transfer limits depend on your account type and KYC level. Check Settings > Limits for details.' },
  { question: 'How do I contact support?', answer: 'Use the "Contact Support" button below or start a live chat from the Chat screen.' },
  { question: 'How long do international transfers take?', answer: 'International transfers typically take 2-5 business days depending on the destination country.' },
  { question: 'How do I dispute a transaction?', answer: 'Go to the transaction details and tap "Dispute Transaction", then follow the prompts.' },
];

export const HelpScreen: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const filteredFaqs = FAQ_ITEMS.filter(
    item => item.question.toLowerCase().includes(searchQuery.toLowerCase()) ||
            item.answer.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <ScrollView style={styles.container}>
      <Text variant="headlineMedium" style={styles.title}>Help & Support</Text>
      <Searchbar placeholder="Search articles..." value={searchQuery} onChangeText={setSearchQuery} style={styles.searchbar} />
      <Text variant="titleMedium" style={styles.subtitle}>Frequently Asked Questions</Text>
      {filteredFaqs.map((item, index) => (
        <List.Accordion key={index} title={item.question}
          expanded={expandedId === index} onPress={() => setExpandedId(expandedId === index ? null : index)}
          style={styles.accordion}>
          <List.Item title="" description={item.answer} descriptionNumberOfLines={10} style={styles.answer} />
        </List.Accordion>
      ))}
      <Divider style={styles.divider} />
      <Button mode="contained" style={styles.button} icon="headset">Contact Support</Button>
      <Button mode="outlined" style={styles.button} icon="chat">Start Live Chat</Button>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, backgroundColor: '#fff' },
  title: { textAlign: 'center', marginVertical: 16 },
  subtitle: { marginVertical: 12 },
  searchbar: { marginBottom: 16 },
  accordion: { backgroundColor: '#f9f9f9' },
  answer: { paddingLeft: 16 },
  divider: { marginVertical: 24 },
  button: { marginBottom: 12 },
});

export default HelpScreen;
