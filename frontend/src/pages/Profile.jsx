import { useState, useEffect } from 'react';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import AppHeader from '../components/AppHeader';

export default function Profile() {
  const { updateUser } = useAuth();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [loadError, setLoadError] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    async function loadProfile() {
      try {
        const res = await api.get('/api/users/me');
        setName(res.data.name);
        setEmail(res.data.email);
        updateUser({ name: res.data.name, email: res.data.email });
      } catch {
        setLoadError('Could not load your profile. Is the server running?');
      }
    }
    loadProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!name.trim() || !email.trim()) {
      setError('Name and email are required.');
      return;
    }

    setSaving(true);
    try {
      const res = await api.put('/api/users/me', { name: name.trim(), email: email.trim() });
      updateUser({ name: res.data.name, email: res.data.email });
      setSuccess('Profile updated.');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not update profile.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <AppHeader variant="full" />
      <main className="page narrow">
        <h1 className="page-title">Profile</h1>
        <p className="page-subtitle">Manage your account details.</p>

        <div className="card">
          <h3>Account information</h3>
          <p className="card-sub">Update your name or email address.</p>
          <form onSubmit={handleSubmit}>
            <div className="field">
              <label htmlFor="fullName">Full name</label>
              <input type="text" id="fullName" value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="field">
              <label htmlFor="email">Email</label>
              <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="form-error">{error || loadError}</div>
            <div className="form-success">{success}</div>
            <button type="submit" className="save-btn" disabled={saving}>
              {saving ? 'Saving…' : 'Save changes'}
            </button>
          </form>
        </div>
      </main>
    </>
  );
}
