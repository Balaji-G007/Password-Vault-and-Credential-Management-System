import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/client';

function friendlyError(err, fallback) {
  if (!err.response) return 'Could not connect to the server. Please check your connection and try again.';
  return err.response?.data?.message || fallback;
}

export default function ForgotPassword() {
  const navigate = useNavigate();
  const [step, setStep] = useState('email'); // 'email' | 'reset' | 'done'
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [emailError, setEmailError] = useState('');
  const [resetError, setResetError] = useState('');
  const [resendMessage, setResendMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSendCode = async (e) => {
    e.preventDefault();
    setEmailError('');
    setSubmitting(true);
    try {
      await api.post('/api/auth/forgot-password', { email: email.trim() });
      setStep('reset');
    } catch (err) {
      setEmailError(friendlyError(err, 'Something went wrong.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleReset = async (e) => {
    e.preventDefault();
    setResetError('');

    if (newPassword.length < 6) {
      setResetError('Password must be at least 6 characters.');
      return;
    }

    setSubmitting(true);
    try {
      await api.post('/api/auth/reset-password', { email, otp: otp.trim(), newPassword });
      setStep('done');
      setTimeout(() => navigate('/login'), 1800);
    } catch (err) {
      setResetError(friendlyError(err, 'Could not reset password.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleResend = async (e) => {
    e.preventDefault();
    setResendMessage('');
    setResetError('');
    try {
      await api.post('/api/auth/forgot-password', { email });
      setResendMessage('Code resent.');
    } catch (err) {
      setResetError(friendlyError(err, 'Could not resend code.'));
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
      <div className="auth-card">
        <div className="eyebrow">Reset password</div>

        {step === 'email' && (
          <>
            <h2>Forgot your master password?</h2>
            <p className="subhead">Enter your account email and we'll send you a one-time code.</p>
            <form onSubmit={handleSendCode}>
              <div className="field">
                <label htmlFor="email">Email</label>
                <input
                  type="email" id="email" placeholder="you@domain.com"
                  value={email} onChange={(e) => setEmail(e.target.value)} required
                />
              </div>
              <div className="error-msg">{emailError}</div>
              <button type="submit" className="submit-btn" disabled={submitting}>
                {submitting ? 'Sending…' : 'Send code'}
              </button>
            </form>
          </>
        )}

        {step === 'reset' && (
          <>
            <h2>Enter your code</h2>
            <p className="subhead">We sent a 6-digit code to {email}. It expires in 10 minutes.</p>
            <form onSubmit={handleReset}>
              <div className="field">
                <label htmlFor="otp">Verification code</label>
                <input
                  type="text" id="otp" placeholder="123456" maxLength={6}
                  value={otp} onChange={(e) => setOtp(e.target.value)} required
                />
              </div>
              <div className="field">
                <label htmlFor="newPassword">New master password</label>
                <input
                  type="password" id="newPassword" placeholder="At least 6 characters"
                  value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required
                />
              </div>
              <div className="error-msg">{resetError}</div>
              {resendMessage && <div className="success-msg">{resendMessage}</div>}
              <button type="submit" className="submit-btn" disabled={submitting}>
                {submitting ? 'Resetting…' : 'Reset password'}
              </button>
            </form>
            <p className="switch-line"><a href="#" className="link" onClick={handleResend}>Resend code</a></p>
          </>
        )}

        {step === 'done' && (
          <p className="success-msg" style={{ textAlign: 'center', marginTop: 16 }}>
            Password reset! Redirecting to sign in…
          </p>
        )}

        <p className="switch-line">Remembered it? <Link to="/login" className="link">Sign in</Link></p>
      </div>
    </div>
  );
}