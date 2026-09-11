import { useState, useEffect } from 'react';
import api from '../api/client';
import AppHeader from '../components/AppHeader';

export default function Reports() {
  const [health, setHealth] = useState(null);
  const [activity, setActivity] = useState(null);

  useEffect(() => {
    api.get('/api/reports/password-health').then((res) => setHealth(res.data)).catch(() => {});
    api.get('/api/reports/login-activity').then((res) => setActivity(res.data)).catch(() => {});
  }, []);

  const total = health?.totalCredentials || 1;

  return (
    <>
      <AppHeader variant="back" />
      <main className="page">
        <h1 className="page-title">📈 Security Reports</h1>

        <div className="report-section">
          <h2>Password Health</h2>
          <div className="health-bar">
            {health && (
              <>
                <div className="bar-strong" style={{ width: `${(health.strong / total) * 100}%` }} />
                <div className="bar-medium" style={{ width: `${(health.medium / total) * 100}%` }} />
                <div className="bar-weak" style={{ width: `${(health.weak / total) * 100}%` }} />
              </>
            )}
          </div>
          <div className="stat-row">
            <div className="stat-box"><div className="label">Total</div><div className="value">{health ? health.totalCredentials : '-'}</div></div>
            <div className="stat-box"><div className="label">Strong</div><div className="value c-strong">{health ? health.strong : '-'}</div></div>
            <div className="stat-box"><div className="label">Medium</div><div className="value c-medium">{health ? health.medium : '-'}</div></div>
            <div className="stat-box"><div className="label">Weak</div><div className="value c-weak">{health ? health.weak : '-'}</div></div>
          </div>
          <div className="stat-row" style={{ marginTop: 12 }}>
            <div className="stat-box" style={{ gridColumn: 'span 4' }}>
              <div className="label">Health Score</div>
              <div className="value c-score">{health ? `${health.healthScore}%` : '-'}</div>
            </div>
          </div>
        </div>

        <div className="report-section">
          <h2>Login Activity</h2>
          <div className="stat-row">
            <div className="stat-box" style={{ gridColumn: 'span 2' }}><div className="label">Total Attempts</div><div className="value">{activity ? activity.totalAttempts : '-'}</div></div>
            <div className="stat-box"><div className="label">Successful</div><div className="value c-strong">{activity ? activity.successfulLogins : '-'}</div></div>
            <div className="stat-box"><div className="label">Failed</div><div className="value c-weak">{activity ? activity.failedLogins : '-'}</div></div>
          </div>
        </div>
      </main>
    </>
  );
}
