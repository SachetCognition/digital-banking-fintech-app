import React, { useState, useEffect } from 'react';
import { View, ScrollView, StyleSheet, Alert } from 'react-native';
import { TextInput, Button, Text, Divider, Switch, Avatar, Card } from 'react-native-paper';
import { useAuth } from '../contexts/AuthContext';
import { api } from '../services/api';

interface UserProfile {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  kycStatus: string;
  mfaEnabled: boolean;
}

export const ProfileScreen: React.FC = () => {
  const { logout } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [phone, setPhone] = useState('');
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => { fetchProfile(); }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get('/api/v1/customers/me');
      setProfile(response.data);
      setPhone(response.data.phone);
    } catch (error) {
      console.error('Failed to fetch profile:', error);
    }
  };

  const handleSave = async () => {
    setLoading(true);
    try {
      await api.put(`/api/v1/customers/${profile?.id}`, { phone });
      Alert.alert('Success', 'Profile updated');
      setEditing(false);
      fetchProfile();
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.message || 'Update failed');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleMfa = async () => {
    try {
      await api.post('/api/v1/auth/mfa/toggle');
      fetchProfile();
    } catch (error) {
      Alert.alert('Error', 'Failed to toggle MFA');
    }
  };

  const handleLogout = () => {
    Alert.alert('Logout', 'Are you sure you want to log out?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Logout', style: 'destructive', onPress: () => logout() },
    ]);
  };

  if (!profile) {
    return <View style={styles.container}><Text>Loading...</Text></View>;
  }

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Avatar.Text size={64} label={profile.fullName.split(' ').map(n => n[0]).join('')} />
        <Text variant="headlineSmall" style={styles.name}>{profile.fullName}</Text>
        <Text variant="bodyMedium" style={styles.email}>{profile.email}</Text>
      </View>

      <Card style={styles.card}>
        <Card.Content>
          <Text variant="titleMedium">KYC Status</Text>
          <Text variant="bodyLarge" style={styles.status}>{profile.kycStatus}</Text>
        </Card.Content>
      </Card>

      <Card style={styles.card}>
        <Card.Content>
          <Text variant="titleMedium">Phone</Text>
          {editing ? (
            <View>
              <TextInput value={phone} onChangeText={setPhone} mode="outlined" style={styles.input} keyboardType="phone-pad" />
              <View style={styles.buttonRow}>
                <Button mode="contained" onPress={handleSave} loading={loading}>Save</Button>
                <Button mode="text" onPress={() => setEditing(false)}>Cancel</Button>
              </View>
            </View>
          ) : (
            <View style={styles.row}>
              <Text variant="bodyLarge">{profile.phone}</Text>
              <Button mode="text" onPress={() => setEditing(true)}>Edit</Button>
            </View>
          )}
        </Card.Content>
      </Card>

      <Card style={styles.card}>
        <Card.Content>
          <View style={styles.row}>
            <Text variant="titleMedium">Two-Factor Authentication</Text>
            <Switch value={profile.mfaEnabled} onValueChange={handleToggleMfa} />
          </View>
        </Card.Content>
      </Card>

      <Divider style={styles.divider} />
      <Button mode="outlined" onPress={handleLogout} style={styles.logoutButton} textColor="#d32f2f">
        Logout
      </Button>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  header: { alignItems: 'center', paddingVertical: 24, backgroundColor: '#fff' },
  name: { marginTop: 12 },
  email: { color: '#666' },
  card: { margin: 8, marginHorizontal: 16 },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  buttonRow: { flexDirection: 'row', gap: 8, marginTop: 8 },
  input: { marginVertical: 8 },
  status: { marginTop: 4, textTransform: 'capitalize' },
  divider: { marginVertical: 16 },
  logoutButton: { marginHorizontal: 16, marginBottom: 32, borderColor: '#d32f2f' },
});

export default ProfileScreen;
