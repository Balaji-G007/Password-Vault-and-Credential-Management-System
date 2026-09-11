import { useState, useEffect } from 'react';
import api from '../api/client';
import AppHeader from '../components/AppHeader';

export default function SecurityDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    api.get('/api/security-dashboard/summary')
      .then((res) => setData(res.data))
      .catch(() => setError(true));
  }, []);

  const s = data?.stats;

  return (
    <>
      <AppHeader variant="back" />
      <main className="page wide">
        <h1 className="page-title">📊 Security Analytics Dashboard</h1>
        <p className="page-subtitle">A complete overview of your account's security activity</p>

        {error && <div className="empty-state">Could not load the dashboard.</div>}

        <div className="stats-grid">
          <div className="stat-card"><div className="stat-label">Total Logins</div><div className="stat-value">{s ? s.totalLogins : '-'}</div></div>
          <div className="stat-card"><div className="stat-label">Successful</div><div className="stat-value v-success">{s ? s.successfulLogins : '-'}</div></div>
          <div className="stat-card"><div className="stat-label">Failed</div><div className="stat-value v-fail">{s ? s.failedLogins : '-'}</div></div>
          <div className="stat-card"><div className="stat-label">Suspicious</div><div className="stat-value v-warn">{s ? s.suspiciousActivityCount : '-'}</div></div>
          <div className="stat-card"><div className="stat-label">Unread Alerts</div><div className="stat-value v-fail">{s ? s.unreadAlertCount : '-'}</div></div>
        </div>

        <div className="panels">
          <div className="panel">
            <h3>Recent Logins</h3>
            {!data && !error && <div className="empty">Loading…</div>}
            {data && data.recentLogins.length === 0 && <div className="empty">No login activity yet.</div>}
            {data && data.recentLogins.map((l, i) => (
              <div className="panel-row" key={i}>
                <span>{new Date(l.attemptedAt).toLocaleString()}</span>
                <span className={`badge ${l.success ? 'badge-success' : 'badge-failed'}`}>{l.success ? 'SUCCESS' : 'FAILED'}</span>
              </div>
            ))}
          </div>

          <div className="panel">
            <h3>Suspicious Activity</h3>
            {!data && !error && <div className="empty">Loading…</div>}
            {data && data.recentSuspiciousActivity.length === 0 && <div className="empty">No suspicious activity detected.</div>}
            {data && data.recentSuspiciousActivity.map((a, i) => (
              <div className="panel-row" key={i}><span>{a.description}</span><span className="badge badge-high">{a.status}</span></div>
            ))}
          </div>

          <div className="panel">
            <h3>Security Alerts</h3>
            {!data && !error && <div className="empty">Loading…</div>}
            {data && data.recentAlerts.length === 0 && <div className="empty">No alerts.</div>}
            {data && data.recentAlerts.map((a, i) => (
              <div className="panel-row" key={i}><span>{a.alertType.replaceAll('_', ' ')}</span><span className="badge badge-high">{a.severity}</span></div>
            ))}
          </div>

          <div className="panel">
            <h3>Recent Audit Activity</h3>
            {!data && !error && <div className="empty">Loading…</div>}
            {data && data.recentAuditLogs.length === 0 && <div className="empty">No audit history yet.</div>}
            {data && data.recentAuditLogs.map((a, i) => (
              <div className="panel-row" key={i}><span>{a.action.replaceAll('_', ' ')}</span><span>{new Date(a.createdAt).toLocaleTimeString()}</span></div>
            ))}
          </div>
        </div>
      </main>
    </>
  );
}
