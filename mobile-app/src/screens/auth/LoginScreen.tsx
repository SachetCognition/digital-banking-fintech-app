import React, { useState } from 'react';
import { View, StyleSheet, ScrollView, Alert } from 'react-native';
import { TextInput, Button, Text, Card, Title } from 'react-native-paper';
import { useAuth } from '../../contexts/AuthContext';

const LoginScreen: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();

  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert('Error', 'Please fill in all fields');
      return;
    }

    try {
      setIsLoading(true);
      await login(email, password);
    } catch (error: any) {
      Alert.alert('Login Failed', error.response?.data?.message || 'An error occurred');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.content}>
        <View style={styles.logoContainer}>
          <View style={styles.logo}>
            <Text style={styles.logoText}>E</Text>
          </View>
          <Text style={styles.bankName}>Emirates Digital Bank</Text>
          <Text style={styles.tagline}>Shariah-Compliant Digital Banking</Text>
        </View>

        <Card style={styles.card}>
          <Card.Content>
            <Title style={styles.title}>Ahlan wa Sahlan</Title>
            <Text style={styles.subtitle}>Sign in to your Emirates Digital account</Text>
            
            <TextInput
              label="Email / Emirates ID"
              value={email}
              onChangeText={setEmail}
              mode="outlined"
              keyboardType="email-address"
              autoCapitalize="none"
              placeholder="ahmed@company.ae"
              style={styles.input}
              outlineColor="#0d2847"
              activeOutlineColor="#d4af37"
            />
            
            <TextInput
              label="Password"
              value={password}
              onChangeText={setPassword}
              mode="outlined"
              secureTextEntry
              style={styles.input}
              outlineColor="#0d2847"
              activeOutlineColor="#d4af37"
            />
            
            <Button
              mode="contained"
              onPress={handleLogin}
              loading={isLoading}
              disabled={isLoading}
              style={styles.button}
              buttonColor="#0d2847"
            >
              Sign In
            </Button>
            
            <Button
              mode="text"
              onPress={() => {}}
              style={styles.forgotButton}
              textColor="#d4af37"
            >
              Forgot Password?
            </Button>
          </Card.Content>
        </Card>

        <Text style={styles.footer}>
          Regulated by the Central Bank of the UAE
        </Text>
        <Text style={styles.footerSub}>
          PCI DSS Certified | ISO 27001 | Shariah Compliant
        </Text>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0a1628',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    padding: 20,
    paddingTop: 60,
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: 32,
  },
  logo: {
    width: 64,
    height: 64,
    borderRadius: 16,
    backgroundColor: '#d4af37',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
  },
  logoText: {
    color: 'white',
    fontSize: 32,
    fontWeight: 'bold',
  },
  bankName: {
    color: 'white',
    fontSize: 22,
    fontWeight: 'bold',
  },
  tagline: {
    color: '#d4af37',
    fontSize: 12,
    marginTop: 4,
    letterSpacing: 1,
  },
  card: {
    elevation: 4,
    borderRadius: 16,
    backgroundColor: 'white',
  },
  title: {
    textAlign: 'center',
    marginBottom: 8,
    fontSize: 24,
    fontWeight: 'bold',
    color: '#0a1628',
  },
  subtitle: {
    textAlign: 'center',
    marginBottom: 24,
    color: '#64748b',
    fontSize: 14,
  },
  input: {
    marginBottom: 16,
    backgroundColor: 'white',
  },
  button: {
    marginTop: 8,
    paddingVertical: 8,
    borderRadius: 12,
  },
  forgotButton: {
    marginTop: 16,
  },
  footer: {
    textAlign: 'center',
    color: '#64748b',
    fontSize: 11,
    marginTop: 24,
  },
  footerSub: {
    textAlign: 'center',
    color: '#4a6fa5',
    fontSize: 10,
    marginTop: 4,
  },
});

export default LoginScreen;
