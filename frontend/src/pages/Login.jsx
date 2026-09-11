import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import AuthDial from '../components/AuthDial';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [emailError, setEmailError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitLabel, setSubmitLabel] = useState('Unlock vault');

   const handleSubmit = async (e) => {
    e.preventDefault();
    setEmailError('');
    setPasswordError('');

    let valid = true;
    if (!EMAIL_RE.test(email)) { setEmailError('Enter a valid email address.'); valid = false; }
    if (!password) { setPasswordError('Master password is required.'); valid = false; }
    if (!valid) return;

    setSubmitting(true);
    setSubmitLabel('Unlocking…');

    try {
      const res = await api.post('/api/auth/login', { email, password });
      const data = res.data;
      setSubmitLabel('Unlocked ✓');
      login(data.token, { name: data.name, email: data.email });
      navigate('/dashboard');
    } catch (err) {
      if (!err.response) {
        setPasswordError('Could not connect to the server. Please check your connection and try again.');
      } else {
        const message = err.response?.data?.message || 'Invalid email or password.';
        setPasswordError(message);
      }
      setSubmitLabel('Unlock vault');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-layout">
      <aside className="auth-brand">
        <div className="brand-mark"><span className="dot"></span>VAULTKEEP</div>

        <AuthDial />

        <div className="auth-brand-copy">
          <h1>Every credential, one combination away.</h1>
          <p>Zero-knowledge encryption keeps your vault unreadable to anyone but you — not even us.</p>
        </div>

        <div className="auth-brand-foot">
          <span>AES-256</span>
          <span>ZERO-KNOWLEDGE</span>
          <span>SOC 2</span>
        </div>
      </aside>

      <main className="auth-panel">
        <div className="auth-card">
          <div className="eyebrow">Sign in</div>
          <h2>Welcome back</h2>
          <p className="subhead">Unlock your vault to access your credentials.</p>

          <form onSubmit={handleSubmit} noValidate>
            <div className="field">
              <label htmlFor="loginEmail">Email</label>
              <div className="input-wrap">
                <input
                  type="email"
                  id="loginEmail"
                  placeholder="you@domain.com"
                  autoComplete="username"
                  className={emailError ? 'invalid' : ''}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              <div className="error-msg">{emailError}</div>
            </div>

            <div className="field">
              <label htmlFor="loginPassword">Master Password</label>
              <div className="input-wrap">
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="loginPassword"
                  placeholder="••••••••••••"
                  autoComplete="current-password"
                  className={passwordError ? 'invalid' : ''}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <button
                  type="button"
                  className="toggle-visibility"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  onClick={() => setShowPassword((v) => !v)}
                >
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6">
                    <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                </button>
              </div>
              <div className="error-msg">{passwordError}</div>
            </div>

            <div className="row-between">
              <label className="checkbox-line">
                <input type="checkbox" /> Keep vault unlocked
              </label>
              <Link to="/forgot-password" className="link">Forgot master password?</Link>
            </div>

            <button type="submit" className="submit-btn" disabled={submitting}>
              {submitLabel}
            </button>

            <div className="status-note">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                <rect x="4" y="10" width="16" height="10" rx="2" />
                <path d="M8 10V7a4 4 0 0 1 8 0v3" />
              </svg>
              <span>Your master password never leaves your device. We only ever see the encrypted vault.</span>
            </div>
          </form>

          <p className="switch-line">
            Don't have a vault yet? <Link to="/register" className="link">Create one</Link>
          </p>
        </div>
      </main>
    </div>
  );
}
