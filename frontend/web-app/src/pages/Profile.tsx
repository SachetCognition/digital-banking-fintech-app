import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface ProfileData {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  address?: { street: string; city: string; emirate: string; poBox: string; country: string; };
  preferredLanguage: string;
}

export default function Profile() {
  const user = (() => {
    try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
  })();

  const [profile, setProfile] = useState<ProfileData>({
    id: user.id || 'demo-001',
    fullName: user.fullName || 'Ahmed Al-Rashid',
    email: user.email || 'ahmed.alrashid@emiratesdigital.ae',
    phone: '+971 50 842 7631',
    address: { street: 'Tower 3, Apt 2401, Al Reem Island', city: 'Abu Dhabi', emirate: 'Abu Dhabi', poBox: 'P.O. Box 45821', country: 'United Arab Emirates' },
    preferredLanguage: 'en',
  });
  const [editing, setEditing] = useState(false);
  const [editData, setEditData] = useState({ ...profile });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    http.get('/api/v1/customers/me').then(res => {
      setProfile(res.data);
      setEditData(res.data);
    }).catch(() => {});
  }, []);

  const handleSave = async () => {
    setMessage(''); setError('');
    try {
      await http.put(`/api/v1/customers/${profile.id}`, editData);
      setProfile(editData);
      setMessage('Profile updated successfully');
      setEditing(false);
    } catch (e: any) {
      setError(e.response?.data?.detail || 'Failed to update profile');
    }
  };

  return (
    <div className="max-w-4xl space-y-6">
      {/* Profile Header */}
      <div className="bg-gradient-to-r from-[#0a1628] via-[#0d2847] to-[#1a3a5c] rounded-2xl p-6 text-white relative overflow-hidden">
        <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZGVmcz48cGF0dGVybiBpZD0iYSIgcGF0dGVyblVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBwYXR0ZXJuVHJhbnNmb3JtPSJyb3RhdGUoNDUpIj48cGF0aCBkPSJNLTEwIDMwaDYwIiBzdHJva2U9InJnYmEoMjEyLDE3NSw1NSwwLjA1KSIgc3Ryb2tlLXdpZHRoPSIxIi8+PC9wYXR0ZXJuPjwvZGVmcz48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSJ1cmwoI2EpIi8+PC9zdmc+')] opacity-50"></div>
        <div className="relative z-10 flex items-center gap-5">
          <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-[#d4af37] to-[#b8960c] flex items-center justify-center text-white font-bold text-3xl shadow-lg shadow-[#d4af37]/20">
            {profile.fullName[0]}
          </div>
          <div>
            <h2 className="text-2xl font-bold">{profile.fullName}</h2>
            <p className="text-slate-400 text-sm mt-0.5">{profile.email}</p>
            <div className="flex items-center gap-3 mt-2">
              <span className="inline-flex items-center gap-1.5 text-xs bg-emerald-500/20 text-emerald-400 px-2.5 py-0.5 rounded-full font-medium">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
                KYC Verified
              </span>
              <span className="inline-flex items-center gap-1.5 text-xs bg-[#d4af37]/20 text-[#d4af37] px-2.5 py-0.5 rounded-full font-medium">
                Gold Tier Customer
              </span>
            </div>
          </div>
        </div>
      </div>

      {message && <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-xl">{message}</div>}
      {error && <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl">{error}</div>}

      <div className="grid lg:grid-cols-2 gap-6">
        {/* Personal Information */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-slate-800">Personal Information</h3>
            {!editing && (
              <button onClick={() => { setEditing(true); setEditData({...profile}); }}
                className="text-sm text-[#0d2847] hover:text-[#d4af37] font-medium">Edit</button>
            )}
          </div>
          <div className="space-y-4">
            {[
              { label: 'Full Name', value: profile.fullName, key: 'fullName', editable: false },
              { label: 'Email', value: profile.email, key: 'email', editable: false },
              { label: 'Mobile', value: profile.phone, key: 'phone', editable: true },
              { label: 'Emirates ID', value: '784-1990-XXXXXXX-2', key: 'emiratesId', editable: false },
              { label: 'Language', value: profile.preferredLanguage === 'ar' ? 'Arabic' : 'English', key: 'preferredLanguage', editable: true },
            ].map((field) => (
              <div key={field.key}>
                <p className="text-xs text-slate-500 uppercase tracking-wide font-medium mb-1">{field.label}</p>
                {editing && field.editable ? (
                  <input
                    className="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37]"
                    value={(editData as any)[field.key] || field.value}
                    onChange={e => setEditData(prev => ({...prev, [field.key]: e.target.value}))}
                  />
                ) : (
                  <p className="text-sm font-medium text-slate-800">{field.value}</p>
                )}
              </div>
            ))}
          </div>
          {editing && (
            <div className="flex gap-3 mt-5">
              <button onClick={handleSave}
                className="px-4 py-2 bg-gradient-to-r from-[#0d2847] to-[#1a4a7a] text-white text-sm font-medium rounded-lg">
                Save Changes
              </button>
              <button onClick={() => setEditing(false)}
                className="px-4 py-2 bg-slate-100 text-slate-600 text-sm font-medium rounded-lg hover:bg-slate-200">
                Cancel
              </button>
            </div>
          )}
        </div>

        {/* Address */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h3 className="font-semibold text-slate-800 mb-4">Registered Address</h3>
          <div className="space-y-4">
            {[
              { label: 'Street', value: profile.address?.street || 'Tower 3, Apt 2401, Al Reem Island' },
              { label: 'City', value: profile.address?.city || 'Abu Dhabi' },
              { label: 'Emirate', value: profile.address?.emirate || 'Abu Dhabi' },
              { label: 'P.O. Box', value: profile.address?.poBox || 'P.O. Box 45821' },
              { label: 'Country', value: profile.address?.country || 'United Arab Emirates' },
            ].map((field) => (
              <div key={field.label}>
                <p className="text-xs text-slate-500 uppercase tracking-wide font-medium mb-1">{field.label}</p>
                <p className="text-sm font-medium text-slate-800">{field.value}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Security */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h3 className="font-semibold text-slate-800 mb-4">Security Settings</h3>
          <div className="space-y-3">
            {[
              { label: 'Two-Factor Authentication', status: 'Enabled', statusColor: 'text-emerald-600 bg-emerald-50' },
              { label: 'Biometric Login', status: 'Active', statusColor: 'text-emerald-600 bg-emerald-50' },
              { label: 'Transaction Alerts', status: 'SMS + Email', statusColor: 'text-blue-600 bg-blue-50' },
              { label: 'Last Password Change', status: '15 days ago', statusColor: 'text-slate-600 bg-slate-100' },
            ].map((item) => (
              <div key={item.label} className="flex items-center justify-between py-2">
                <span className="text-sm text-slate-600">{item.label}</span>
                <span className={`text-xs font-medium px-2.5 py-1 rounded-full ${item.statusColor}`}>{item.status}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Account Preferences */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h3 className="font-semibold text-slate-800 mb-4">Account Preferences</h3>
          <div className="space-y-3">
            {[
              { label: 'Default Currency', value: 'AED - UAE Dirham' },
              { label: 'Statement Frequency', value: 'Monthly' },
              { label: 'Notification Channel', value: 'Push + SMS + Email' },
              { label: 'Account Type', value: 'Premium Individual' },
              { label: 'Relationship Manager', value: 'Fatima Al-Maktoum' },
            ].map((item) => (
              <div key={item.label} className="flex items-center justify-between py-2">
                <span className="text-sm text-slate-600">{item.label}</span>
                <span className="text-sm font-medium text-slate-800">{item.value}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
