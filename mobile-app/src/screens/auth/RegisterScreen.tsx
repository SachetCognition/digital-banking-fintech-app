import React, { useState } from 'react';
import { View, ScrollView, StyleSheet, Alert } from 'react-native';
import { TextInput, Button, Text, HelperText } from 'react-native-paper';
import { useNavigation } from '@react-navigation/native';
import { api } from '../../services/api';

export const RegisterScreen: React.FC = () => {
  const navigation = useNavigation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState('');
  const [country, setCountry] = useState('');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = 'Valid email is required';
    }
    if (!password || password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    }
    if (password !== confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }
    if (!fullName.trim()) {
      newErrors.fullName = 'Full name is required';
    }
    if (!phone || !/^\+?[\d\s-]{10,}$/.test(phone)) {
      newErrors.phone = 'Valid phone number is required';
    }
    if (!dateOfBirth) {
      newErrors.dateOfBirth = 'Date of birth is required';
    }
    if (!country.trim()) {
      newErrors.country = 'Country is required';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRegister = async () => {
    if (!validate()) return;
    setLoading(true);
    try {
      await api.post('/api/v1/auth/register', {
        email, password, fullName, phone, dateOfBirth, country,
      });
      Alert.alert('Success', 'Registration successful. Please log in.', [
        { text: 'OK', onPress: () => navigation.navigate('Login' as never) },
      ]);
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text variant="headlineMedium" style={styles.title}>Open an Account</Text>
      <Text style={styles.subtitle}>Emirates Digital Bank - Shariah Compliant</Text>
      <TextInput label="Full Name (as per Emirates ID)" value={fullName} onChangeText={setFullName} mode="outlined" style={styles.input} placeholder="Ahmed Mohammed Al-Rashid" error={!!errors.fullName} />
      {errors.fullName && <HelperText type="error">{errors.fullName}</HelperText>}
      <TextInput label="Email" value={email} onChangeText={setEmail} mode="outlined" style={styles.input} keyboardType="email-address" autoCapitalize="none" placeholder="ahmed@company.ae" error={!!errors.email} />
      {errors.email && <HelperText type="error">{errors.email}</HelperText>}
      <TextInput label="Mobile Number" value={phone} onChangeText={setPhone} mode="outlined" style={styles.input} keyboardType="phone-pad" placeholder="+971 50 123 4567" error={!!errors.phone} />
      {errors.phone && <HelperText type="error">{errors.phone}</HelperText>}
      <TextInput label="Date of Birth (YYYY-MM-DD)" value={dateOfBirth} onChangeText={setDateOfBirth} mode="outlined" style={styles.input} error={!!errors.dateOfBirth} />
      {errors.dateOfBirth && <HelperText type="error">{errors.dateOfBirth}</HelperText>}
      <TextInput label="Nationality" value={country} onChangeText={setCountry} mode="outlined" style={styles.input} placeholder="United Arab Emirates" error={!!errors.country} />
      {errors.country && <HelperText type="error">{errors.country}</HelperText>}
      <TextInput label="Password" value={password} onChangeText={setPassword} mode="outlined" style={styles.input} secureTextEntry error={!!errors.password} />
      {errors.password && <HelperText type="error">{errors.password}</HelperText>}
      <TextInput label="Confirm Password" value={confirmPassword} onChangeText={setConfirmPassword} mode="outlined" style={styles.input} secureTextEntry error={!!errors.confirmPassword} />
      {errors.confirmPassword && <HelperText type="error">{errors.confirmPassword}</HelperText>}
      <Button mode="contained" onPress={handleRegister} loading={loading} disabled={loading} style={styles.button} buttonColor="#0d2847">
        Open Account
      </Button>
      <Button mode="text" onPress={() => navigation.navigate('Login' as never)} style={styles.linkButton} textColor="#d4af37">
        Already have an account? Sign In
      </Button>
      <Text style={styles.footer}>Regulated by the Central Bank of the UAE</Text>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, backgroundColor: '#fff' },
  title: { textAlign: 'center', marginTop: 24, marginBottom: 4, color: '#0a1628' },
  subtitle: { textAlign: 'center', marginBottom: 20, color: '#d4af37', fontSize: 13 },
  input: { marginBottom: 8, backgroundColor: '#fff' },
  button: { marginTop: 16, paddingVertical: 4, borderRadius: 12 },
  linkButton: { marginTop: 8 },
  footer: { textAlign: 'center', color: '#94a3b8', fontSize: 11, marginTop: 16, marginBottom: 24 },
});

export default RegisterScreen;
