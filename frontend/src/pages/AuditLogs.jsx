import { useState, useEffect } from 'react';
import api from '../api/client';
import AppHeader from '../components/AppHeader';

export default function AuditLogs() {
  const [logs, setLogs] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    api.get('/api/audit-logs/me')
      .then((res) => setLogs(res.data))
      .catch(() => setError(true));
  }, []);

  return (
    <>
      <AppHeader variant="back" />
      <main className="page">
        <h1 className="page-title">📋 Audit Logs</h1>
        <p className="page-subtitle">Full history of security-relevant actions on your account</p>
        <table>
          <thead><tr><th>Action</th><th>Details</th><th>Time</th></tr></thead>
          <tbody>
            {logs === null && !error && <tr><td colSpan={3}>Loading…</td></tr>}
            {error && <tr><td colSpan={3}>Failed to load.</td></tr>}
            {logs && logs.length === 0 && <tr><td colSpan={3}>No audit history yet.</td></tr>}
            {logs && logs.map((l, i) => (
              <tr key={i}>
                <td><span className="badge">{l.action.replaceAll('_', ' ')}</span></td>
                <td>{l.details || '—'}</td>
                <td>{new Date(l.createdAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </main>
    </>
  );
}
