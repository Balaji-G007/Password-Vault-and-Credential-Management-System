import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/client';
import AuthDial from '../components/AuthDial';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const STRENGTH_COLORS = ['#E2685B', '#E2A85B', '#D9C24F', '#4FD1C5'];
const STRENGTH_WORDS = ['Weak', 'Fair', 'Good', 'Strong'];

function scorePassword(pw) {
  let score = 0;
  if (pw.length >= 12) score++;
  if (pw.length >= 16) score++;
  if (/[a-z]/.test(pw) && /[A-Z]/.test(pw)) score++;
  if (/\d/.test(pw)) score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;
  return Math.min(score, 4);
}

export default function Register() {
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [ack, setAck] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [submitLabel, setSubmitLabel] = useState('Create vault');

  const strengthScore = password.length ? Math.max(scorePassword(password), 1) : 0;
  const handleSubmit = async (e) => {
    e.preventDefault();
    const nextErrors = {};

    if (name.trim().length < 2) nextErrors.name = 'Enter your name.';
    if (!EMAIL_RE.test(email)) nextErrors.email = 'Enter a valid email address.';
    if (password.length < 6) nextErrors.password = 'Use at least 6 characters.';
    if (confirm !== password || !confirm) nextErrors.confirm = 'Passwords do not match.';

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0 || !ack) return;

    setSubmitting(true);
    setSubmitLabel('Creating vault…');

    try {
      await api.post('/api/auth/register', { name: name.trim(), email: email.trim(), password });
      setSubmitLabel('Vault created ✓');
      navigate('/login');
    } catch (err) {
      if (!err.response) {
        setErrors({ email: 'Could not connect to the server. Please check your connection and try again.' });
      } else {
        const message = err.response?.data?.message || 'Could not create vault.';
        setErrors({ email: message });
      }
      setSubmitLabel('Create vault');
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
          <div className="eyebrow">Create vault</div>
          <h2>Set up your vault</h2>
          <p className="subhead">This master password is the only key — choose carefully.</p>

          <form onSubmit={handleSubmit} noValidate>
            <div className="field">
              <label htmlFor="regName">Full name</label>
              <div className="input-wrap">
                <input
                  type="text" id="regName" placeholder="Jordan Blake" autoComplete="name"
                  className={errors.name ? 'invalid' : ''}
                  value={name} onChange={(e) => setName(e.target.value)} required
                />
              </div>
              <div className="error-msg">{errors.name}</div>
            </div>

            <div className="field">
              <label htmlFor="regEmail">Email</label>
              <div className="input-wrap">
                <input
                  type="email" id="regEmail" placeholder="you@domain.com" autoComplete="username"
                  className={errors.email ? 'invalid' : ''}
                  value={email} onChange={(e) => setEmail(e.target.value)} required
                />
              </div>
              <div className="error-msg">{errors.email}</div>
            </div>

            <div className="field">
              <label htmlFor="regPassword">Master password</label>
              <div className="input-wrap">
                <input
                  type={showPassword ? 'text' : 'password'} id="regPassword"
                  placeholder="At least 6 characters" autoComplete="new-password"
                  className={errors.password ? 'invalid' : ''}
                  value={password} onChange={(e) => setPassword(e.target.value)} required
                />
                <button type="button" className="toggle-visibility" aria-label={showPassword ? 'Hide password' : 'Show password'} onClick={() => setShowPassword((v) => !v)}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" /><circle cx="12" cy="12" r="3" /></svg>
                </button>
              </div>
              <div className="strength">
                {[0, 1, 2, 3].map((i) => (
                  <i key={i} style={{ background: i < strengthScore ? STRENGTH_COLORS[strengthScore - 1] : 'var(--border)' }} />
                ))}
              </div>
              <div className="strength-label" style={{ color: password ? STRENGTH_COLORS[strengthScore - 1] : 'var(--text-faint)' }}>
                {password ? STRENGTH_WORDS[strengthScore - 1] : '\u00A0'}
              </div>
              <div className="error-msg">{errors.password}</div>
            </div>

            <div className="field">
              <label htmlFor="regConfirm">Confirm master password</label>
              <div className="input-wrap">
                <input
                  type={showConfirm ? 'text' : 'password'} id="regConfirm"
                  placeholder="Re-enter password" autoComplete="new-password"
                  className={errors.confirm ? 'invalid' : ''}
                  value={confirm} onChange={(e) => setConfirm(e.target.value)} required
                />
                <button type="button" className="toggle-visibility" aria-label={showConfirm ? 'Hide password' : 'Show password'} onClick={() => setShowConfirm((v) => !v)}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" /><circle cx="12" cy="12" r="3" /></svg>
                </button>
              </div>
              <div className="error-msg">{errors.confirm}</div>
            </div>

            <label className="checkbox-line">
              <input type="checkbox" checked={ack} onChange={(e) => setAck(e.target.checked)} required />
              I understand Vaultkeep cannot recover this password if lost.
            </label>

            <button type="submit" className="submit-btn" disabled={submitting}>
              {submitLabel}
            </button>
          </form>

          <p className="switch-line">
            Already have a vault? <Link to="/login" className="link">Sign in</Link>
          </p>
        </div>
      </main>
    </div>
  );
}
