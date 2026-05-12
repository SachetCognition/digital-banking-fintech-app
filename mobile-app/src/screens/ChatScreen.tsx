import React, { useState, useEffect, useRef } from 'react';
import { View, FlatList, StyleSheet, KeyboardAvoidingView, Platform } from 'react-native';
import { TextInput, IconButton, Text, Chip } from 'react-native-paper';
import { api } from '../services/api';

interface Message {
  id: string;
  text: string;
  sender: 'user' | 'agent';
  timestamp: string;
}

export const ChatScreen: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState('');
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(false);
  const flatListRef = useRef<FlatList>(null);

  useEffect(() => {
    initializeChat();
  }, []);

  const initializeChat = async () => {
    try {
      const response = await api.get('/api/v1/chat/history');
      setMessages(response.data || []);
      setConnected(true);
    } catch (error) {
      setConnected(false);
      setMessages([{
        id: '1',
        text: 'Welcome! How can we help you today?',
        sender: 'agent',
        timestamp: new Date().toISOString(),
      }]);
    }
  };

  const sendMessage = async () => {
    if (!inputText.trim()) return;
    const userMessage: Message = {
      id: Date.now().toString(),
      text: inputText,
      sender: 'user',
      timestamp: new Date().toISOString(),
    };
    setMessages(prev => [...prev, userMessage]);
    setInputText('');
    setLoading(true);

    try {
      const response = await api.post('/api/v1/chat/send', { message: inputText });
      if (response.data?.reply) {
        const agentMessage: Message = {
          id: (Date.now() + 1).toString(),
          text: response.data.reply,
          sender: 'agent',
          timestamp: new Date().toISOString(),
        };
        setMessages(prev => [...prev, agentMessage]);
      }
    } catch (error) {
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        text: 'Unable to send message. Please try again.',
        sender: 'agent',
        timestamp: new Date().toISOString(),
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  const renderMessage = ({ item }: { item: Message }) => (
    <View style={[styles.messageBubble, item.sender === 'user' ? styles.userMessage : styles.agentMessage]}>
      <Text style={item.sender === 'user' ? styles.userText : styles.agentText}>{item.text}</Text>
      <Text style={styles.timestamp}>{new Date(item.timestamp).toLocaleTimeString()}</Text>
    </View>
  );

  return (
    <KeyboardAvoidingView style={styles.container} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <View style={styles.statusBar}>
        <Chip icon={connected ? 'check-circle' : 'alert-circle'} compact>
          {connected ? 'Connected' : 'Offline'}
        </Chip>
      </View>
      <FlatList
        ref={flatListRef}
        data={messages}
        keyExtractor={(item) => item.id}
        renderItem={renderMessage}
        onContentSizeChange={() => flatListRef.current?.scrollToEnd()}
        contentContainerStyle={styles.messageList}
      />
      <View style={styles.inputContainer}>
        <TextInput value={inputText} onChangeText={setInputText} mode="outlined"
          placeholder="Type a message..." style={styles.input}
          onSubmitEditing={sendMessage} returnKeyType="send" />
        <IconButton icon="send" mode="contained" onPress={sendMessage} disabled={loading || !inputText.trim()} />
      </View>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  statusBar: { padding: 8, alignItems: 'center', backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: '#eee' },
  messageList: { padding: 16 },
  messageBubble: { maxWidth: '80%', padding: 12, borderRadius: 12, marginBottom: 8 },
  userMessage: { alignSelf: 'flex-end', backgroundColor: '#1976d2' },
  agentMessage: { alignSelf: 'flex-start', backgroundColor: '#fff', borderWidth: 1, borderColor: '#e0e0e0' },
  userText: { color: '#fff' },
  agentText: { color: '#333' },
  timestamp: { fontSize: 10, color: '#999', marginTop: 4, textAlign: 'right' },
  inputContainer: { flexDirection: 'row', padding: 8, backgroundColor: '#fff', alignItems: 'center' },
  input: { flex: 1, marginRight: 8 },
});

export default ChatScreen;
