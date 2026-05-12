import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface ProfileData {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  address?: {
    street: string;
    city: string;
    state: string;
    zip: string;
    country: string;
  };
  preferredLanguage: string;
}

export default function Profile() {
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [editing, setEditing] = useState(false);
  const [phone, setPhone] = useState('');
  const [street, setStreet] = useState('');
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [zip, setZip] = useState('');
  const [country, setCountry] = useState('');
  const [preferredLanguage, setPreferredLanguage] = useState('en');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await http.get('/api/v1/customers/me');
      const data = response.data;
      setProfile(data);
      setPhone(data.phone || '');
      setStreet(data.address?.street || '');
      setCity(data.address?.city || '');
      setState(data.address?.state || '');
      setZip(data.address?.zip || '');
      setCountry(data.address?.country || '');
      setPreferredLanguage(data.preferredLanguage || 'en');
    } catch (e) {
      setError('Failed to load profile');
    }
  };

  const handleSave = async () => {
    if (!profile) return;
    setMessage('');
    setError('');
    try {
      await http.put(`/api/v1/customers/${profile.id}`, {
        phone,
        address: { street, city, state, zip, country },
        preferredLanguage,
      });
      setMessage('Profile updated successfully');
      setEditing(false);
      fetchProfile();
    } catch (e: any) {
      setError(e.response?.data?.detail || 'Failed to update profile');
    }
  };

  if (!profile) {
    return <div>Loading profile...</div>;
  }

  return (
    <div style={{ maxWidth: 600 }}>
      <h2>My Profile</h2>
      {message && <div style={{ padding: 12, background: '#d4edda', borderRadius: 4, marginBottom: 16 }}>{message}</div>}
      {error && <div style={{ padding: 12, background: '#f8d7da', borderRadius: 4, marginBottom: 16 }}>{error}</div>}

      <div style={{ marginBottom: 16 }}>
        <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>Name</label>
        <div>{profile.fullName}</div>
      </div>
      <div style={{ marginBottom: 16 }}>
        <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>Email</label>
        <div>{profile.email}</div>
      </div>

      {editing ? (
        <>
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>Phone</label>
            <input value={phone} onChange={e => setPhone(e.target.value)} style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }} />
          </div>
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>Street</label>
            <input value={street} onChange={e => setStreet(e.target.value)} style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }} />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>
            <div>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>City</label>
              <input value={city} onChange={e => setCity(e.target.value)} style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }} />
            </div>
            <div>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>State</label>
              <input value={state} onChange={e => setState(e.target.value)} style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }} />
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>
            <div>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>ZIP</label>
              <input value={zip} onChange={e => setZip(e.target.value)} style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }} />
            </div>
            <div>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>Country</label>
              <input value={country} onChange={e => setCountry(e.target.value)} style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }} />
            </div>
          </div>
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>Preferred Language</label>
            <select value={preferredLanguage} onChange={e => setPreferredLanguage(e.target.value)} style={{ padding: 8, border: '1px solid #ccc', borderRadius: 4 }}>
              <option value="en">English</option>
              <option value="es">Spanish</option>
            </select>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button onClick={handleSave} style={{ padding: '10px 20px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}>Save</button>
            <button onClick={() => setEditing(false)} style={{ padding: '10px 20px', background: '#f1f5f9', border: '1px solid #ccc', borderRadius: 4, cursor: 'pointer' }}>Cancel</button>
          </div>
        </>
      ) : (
        <>
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>Phone</label>
            <div>{profile.phone || 'Not set'}</div>
          </div>
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 4 }}>Address</label>
            <div>{profile.address ? `${profile.address.street}, ${profile.address.city}, ${profile.address.state} ${profile.address.zip}, ${profile.address.country}` : 'Not set'}</div>
          </div>
          <button onClick={() => setEditing(true)} style={{ padding: '10px 20px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}>Edit Profile</button>
        </>
      )}
    </div>
  );
}
