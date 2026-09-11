import { useState, useEffect } from 'react';
import api from '../api/client';
import AppHeader from '../components/AppHeader';

export default function SecurityAlerts() {
  const [alerts, setAlerts] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    api.get('/api/security-alerts/me')
      .then((res) => setAlerts(res.data))
      .catch(() => setError(true));
  }, []);

  return (
    <>
      <AppHeader variant="back" />
      <main className="page">
        <h1 className="page-title">⚠️ Security Alerts</h1>
        <p className="page-subtitle">Alerts generated from detected suspicious activity on your account</p>

        {alerts === null && !error && <div className="empty-state">Loading…</div>}
        {error && <div className="empty-state">Failed to load alerts.</div>}
        {alerts && alerts.length === 0 && (
          <div className="empty-state">No security alerts. Your account looks healthy.</div>
        )}
        {alerts && alerts.map((a, i) => (
          <div key={i} className={`alert-card severity-${a.severity.toLowerCase()}`}>
            <div className="alert-top">
              <span className="alert-type">{a.alertType.replaceAll('_', ' ')}</span>
              <span className={`severity-badge severity-${a.severity}`}>{a.severity}</span>
            </div>
            <div className="alert-message">{a.message}</div>
            <div className="alert-meta">
              <span>{new Date(a.updatedAt || a.createdAt).toLocaleString()}</span>
              <span className="status-badge">{a.status}</span>
            </div>
          </div>
        ))}
      </main>
    </>
  );
}
