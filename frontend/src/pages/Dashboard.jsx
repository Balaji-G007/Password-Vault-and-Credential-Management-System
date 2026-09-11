import { useState, useEffect, useCallback } from 'react';
import api from '../api/client';
import AppHeader from '../components/AppHeader';

function generateStrongPassword(length = 16) {
  const lower = 'abcdefghijklmnopqrstuvwxyz';
  const upper = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const numbers = '0123456789';
  const symbols = '!@#$%^&*()_+-=[]{}?';
  const all = lower + upper + numbers + symbols;

  function randomChar(charset) {
    const arr = new Uint32Array(1);
    crypto.getRandomValues(arr);
    return charset[arr[0] % charset.length];
  }

  let pwd = [randomChar(lower), randomChar(upper), randomChar(numbers), randomChar(symbols)];
  for (let i = pwd.length; i < length; i++) pwd.push(randomChar(all));

  for (let i = pwd.length - 1; i > 0; i--) {
    const arr = new Uint32Array(1);
    crypto.getRandomValues(arr);
    const j = arr[0] % (i + 1);
    [pwd[i], pwd[j]] = [pwd[j], pwd[i]];
  }
  return pwd.join('');
}

// Turns any axios error into a plain, user-friendly sentence.
function friendlyError(err, fallback) {
  if (!err.response) return 'Could not connect to the server. Please check your connection and try again.';
  return err.response?.data?.message || fallback;
}

function CredentialRow({ cred, onChanged }) {
  const [showPw, setShowPw] = useState(false);
  const [showShareForm, setShowShareForm] = useState(false);
  const [shareEmail, setShareEmail] = useState('');
  const [shareHours, setShareHours] = useState('24');
  const [shareError, setShareError] = useState('');
  const [shareSuccess, setShareSuccess] = useState('');
  const [sharing, setSharing] = useState(false);
  const [rowError, setRowError] = useState('');

  const handleDelete = async () => {
    if (!confirm(`Delete the saved credential for "${cred.websiteName}"?`)) return;
    setRowError('');
    try {
      await api.delete(`/api/credentials/${cred.id}`);
      onChanged();
    } catch (err) {
      setRowError(friendlyError(err, 'Could not delete credential.'));
    }
  };

  const handleEdit = async () => {
    const newWebsite = prompt('Website name:', cred.websiteName);
    if (newWebsite === null) return;
    const newUsername = prompt('Username / email:', cred.username);
    if (newUsername === null) return;
    const newPassword = prompt('Password:', cred.password);
    if (newPassword === null) return;

    setRowError('');
    try {
      await api.put(`/api/credentials/${cred.id}`, {
        websiteName: newWebsite,
        username: newUsername,
        password: newPassword,
      });
      onChanged();
    } catch (err) {
      setRowError(friendlyError(err, 'Could not update credential.'));
    }
  };

  const handleShare = async (e) => {
    e.preventDefault();
    setShareError('');
    setShareSuccess('');

    if (!shareEmail.trim()) {
      setShareError('Recipient email is required.');
      return;
    }

    setSharing(true);
    try {
      await api.post('/api/shared-credentials', {
        credentialId: cred.id,
        shareWithEmail: shareEmail.trim(),
        expiresInHours: shareHours ? Number(shareHours) : null,
      });
      setShareSuccess(`Shared with ${shareEmail.trim()}.`);
      setShareEmail('');
    } catch (err) {
      setShareError(friendlyError(err, 'Could not share credential.'));
    } finally {
      setSharing(false);
    }
  };

  return (
    <div className="cred-row-wrapper">
      <div className="cred-row">
        <div className="cred-field">
          <span className="cred-label">Website</span>
          <span className="cred-value website">{cred.websiteName}</span>
        </div>
        <div className="cred-field">
          <span className="cred-label">Username</span>
          <span className="cred-value">{cred.username || '—'}</span>
        </div>
        <div className="cred-field">
          <span className="cred-label">Password</span>
          <div className="pw-row">
            <span className="cred-value pw-text">{showPw ? cred.password : '••••••••'}</span>
            <button type="button" className="toggle-pw" aria-label="Show password" onClick={() => setShowPw((v) => !v)}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6">
                <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" /><circle cx="12" cy="12" r="3" />
              </svg>
            </button>
          </div>
        </div>
        <div className="row-actions">
          <button
            type="button"
            className="icon-btn share-btn"
            aria-label="Share"
            onClick={() => setShowShareForm((v) => !v)}
            title="Share this credential"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
              <circle cx="18" cy="5" r="3" /><circle cx="6" cy="12" r="3" /><circle cx="18" cy="19" r="3" />
              <path d="M8.59 13.51l6.83 3.98" /><path d="M15.41 6.51l-6.82 3.98" />
            </svg>
          </button>
          <button type="button" className="icon-btn edit-btn" aria-label="Edit" onClick={handleEdit}>
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
              <path d="M12 20h9" /><path d="M16.5 3.5a2.12 2.12 0 013 3L7 19l-4 1 1-4L16.5 3.5z" />
            </svg>
          </button>
          <button type="button" className="icon-btn delete-btn" aria-label="Delete" onClick={handleDelete}>
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
              <path d="M3 6h18" /><path d="M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2" /><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" />
            </svg>
          </button>
        </div>
      </div>

      {rowError && <div className="form-error" style={{ padding: '8px 16px' }}>{rowError}</div>}

      {showShareForm && (
        <form className="share-form" onSubmit={handleShare}>
          <div className="field">
            <label htmlFor={`shareEmail-${cred.id}`}>Share with (email)</label>
            <input
              type="email"
              id={`shareEmail-${cred.id}`}
              placeholder="friend@example.com"
              value={shareEmail}
              onChange={(e) => setShareEmail(e.target.value)}
              required
            />
          </div>
          <div className="field">
            <label htmlFor={`shareHours-${cred.id}`}>Expires in (hours)</label>
            <input
              type="number"
              id={`shareHours-${cred.id}`}
              placeholder="24"
              value={shareHours}
              onChange={(e) => setShareHours(e.target.value)}
              min="1"
            />
          </div>
          <button type="submit" className="save-btn" disabled={sharing}>
            {sharing ? 'Sharing…' : 'Share'}
          </button>
          {shareError && <div className="form-error">{shareError}</div>}
          {shareSuccess && <div className="form-success">{shareSuccess}</div>}
        </form>
      )}
    </div>
  );
}

export default function Dashboard() {
  const [credentials, setCredentials] = useState(null);
  const [loadError, setLoadError] = useState('');

  const [websiteName, setWebsiteName] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [addError, setAddError] = useState('');
  const [saving, setSaving] = useState(false);

  const loadVault = useCallback(async () => {
    try {
      const res = await api.get('/api/credentials');
      setCredentials(res.data);
      setLoadError('');
    } catch (err) {
      setLoadError(friendlyError(err, 'Could not load your vault.'));
    }
  }, []);

  useEffect(() => { loadVault(); }, [loadVault]);

  const handleGenerate = () => setPassword(generateStrongPassword(16));

  const handleAdd = async (e) => {
    e.preventDefault();
    setAddError('');
    if (!websiteName.trim() || !password) {
      setAddError('Website name and password are required.');
      return;
    }

    setSaving(true);
    try {
      await api.post('/api/credentials', {
        websiteName: websiteName.trim(),
        username: username.trim(),
        password,
      });
      setWebsiteName('');
      setUsername('');
      setPassword('');
      loadVault();
    } catch (err) {
      setAddError(friendlyError(err, 'Could not save credential.'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <AppHeader variant="full" />
      <main className="page">
        <h1 className="page-title">Your vault</h1>
        <p className="page-subtitle">Every saved credential, encrypted at rest.</p>

        <div className="add-card">
          <h3>Add a new credential</h3>
          <form className="add-form" onSubmit={handleAdd}>
            <div className="field">
              <label htmlFor="websiteName">Website</label>
              <input type="text" id="websiteName" placeholder="Gmail" value={websiteName} onChange={(e) => setWebsiteName(e.target.value)} required />
            </div>
            <div className="field">
              <label htmlFor="username">Username / Email</label>
              <input type="text" id="username" placeholder="you@gmail.com" value={username} onChange={(e) => setUsername(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="password">Password</label>
              <div className="pw-input-row">
                <input type="text" id="password" placeholder="Password to save" value={password} onChange={(e) => setPassword(e.target.value)} required />
                <button type="button" className="gen-btn" title="Generate strong password" onClick={handleGenerate}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                    <path d="M23 4v6h-6" /><path d="M1 20v-6h6" /><path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" />
                  </svg>
                </button>
              </div>
            </div>
            <button type="submit" className="save-btn" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
          </form>
          <div className="form-error">{addError}</div>
        </div>

        <div className="vault-list">
          {credentials === null && !loadError && <div className="empty-state">Loading your vault…</div>}
          {loadError && <div className="empty-state">{loadError}</div>}
          {credentials && credentials.length === 0 && (
            <div className="empty-state">No saved credentials yet. Add your first one above.</div>
          )}
          {credentials && credentials.length > 0 && credentials.map((cred) => (
            <CredentialRow key={cred.id} cred={cred} onChanged={loadVault} />
          ))}
        </div>
      </main>
    </>
  );
}