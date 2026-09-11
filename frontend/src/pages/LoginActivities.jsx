import { useState, useEffect } from 'react';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import AppHeader from '../components/AppHeader';

export default function LoginActivities() {
  const { user } = useAuth();
  const [logs, setLogs] = useState(null);
  const [error, setError] = useState(false);
  const [suspiciousFlag, setSuspiciousFlag] = useState(null);

  useEffect(() => {
    api.get('/api/login-logs/me')
      .then((res) => {
        setLogs(res.data);
        return api.get('/api/security/suspicious-activity');
      })
      .then((res) => {
        const myFlag = res.data.find((f) => f.email === user?.email);
        if (myFlag) setSuspiciousFlag(myFlag);
      })
      .catch(() => setError(true));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const total = logs ? logs.length : 0;
  const successCount = logs ? logs.filter((l) => l.success).length : 0;
  const failedCount = total - successCount;

  return (
    <>
      <AppHeader variant="back" />
      <main className="page">
        <h1 className="page-title">🔒 Login Activities</h1>
        <p className="page-subtitle">Monitor and review your account login activity</p>

        {suspiciousFlag && (
          <div className="suspicious-banner">
            ⚠️ <span>
              <strong>Suspicious activity detected:</strong> {suspiciousFlag.failedAttempts} failed login
              attempts in the last {suspiciousFlag.windowMinutes} minutes. If this wasn't you, consider
              changing your password.
            </span>
          </div>
        )}

        <div className="stats">
          <div className="stat-card">
            <div className="stat-label">Total Attempts</div>
            <div className="stat-value">{logs ? total : '-'}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Successful Logins</div>
            <div className="stat-value success-value">{logs ? successCount : '-'}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Failed Attempts</div>
            <div className="stat-value failed-value">{logs ? failedCount : '-'}</div>
          </div>
        </div>

        <table>
          <thead><tr><th>#</th><th>Email</th><th>Status</th><th>Date & Time</th></tr></thead>
          <tbody>
            {logs === null && !error && <tr><td colSpan={4}>Loading…</td></tr>}
            {error && <tr><td colSpan={4}>Failed to load login activity.</td></tr>}
            {logs && logs.length === 0 && <tr><td colSpan={4}>No login activity yet.</td></tr>}
            {logs && logs.map((log, i) => (
              <tr key={i}>
                <td>{i + 1}</td>
                <td>{log.email}</td>
                <td><span className={`badge ${log.success ? 'badge-success' : 'badge-failed'}`}>{log.success ? '✓ SUCCESS' : '✗ FAILED'}</span></td>
                <td>{new Date(log.attemptedAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </main>
    </>
  );
}
