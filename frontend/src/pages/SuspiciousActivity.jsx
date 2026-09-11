import { useState, useEffect } from 'react';
import api from '../api/client';
import AppHeader from '../components/AppHeader';

export default function SuspiciousActivity() {
  const [items, setItems] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    api.get('/api/security/suspicious-activity')
      .then((res) => setItems(res.data))
      .catch(() => setError(true));
  }, []);

  return (
    <>
      <AppHeader variant="back" />
      <main className="page">
        <h1 className="page-title">🔍 Suspicious Activity</h1>
        <p className="page-subtitle">Unusual patterns detected on your account</p>
        <table>
          <thead><tr><th>Activity</th><th>Description</th><th>Detected</th><th>Status</th></tr></thead>
          <tbody>
            {items === null && !error && <tr><td colSpan={4}>Loading…</td></tr>}
            {error && <tr><td colSpan={4}>Failed to load.</td></tr>}
            {items && items.length === 0 && <tr><td colSpan={4}>No suspicious activity detected.</td></tr>}
            {items && items.map((a, i) => (
              <tr key={i}>
                <td><span className="badge">{a.activityType.replaceAll('_', ' ')}</span></td>
                <td>{a.description}</td>
                <td>{new Date(a.detectedAt).toLocaleString()}</td>
                <td>{a.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </main>
    </>
  );
}
