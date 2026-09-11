import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import NotificationBell from './NotificationBell';

// variant="full"  -> Dashboard/Profile style header with user info + Security dropdown + logout
// variant="back"  -> simple header with just a "← Dashboard" link (used on the security sub-pages)
export default function AppHeader({ variant = 'back' }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="app-header">
      <div className="brand-mark"><span className="dot"></span>VAULTKEEP</div>

      {variant === 'back' ? (
        <Link to="/dashboard" className="back-link">← Dashboard</Link>
      ) : (
        <div className="user-info">
          <span>{user ? `${user.name} (${user.email})` : 'Loading…'}</span>
          <NotificationBell />
          <Link to="/profile">Profile</Link>
          <div className="dropdown">
            <button className="dropdown-toggle" onClick={() => setMenuOpen((v) => !v)}>
              Security ▾
            </button>
            {menuOpen && (
              <div className="dropdown-menu" onMouseLeave={() => setMenuOpen(false)}>
                <Link to="/login-activities" onClick={() => setMenuOpen(false)}>Login Activities</Link>
                <Link to="/security-alerts" onClick={() => setMenuOpen(false)}>Security Alerts</Link>
                <Link to="/suspicious-activity" onClick={() => setMenuOpen(false)}>Suspicious Activity</Link>
                <Link to="/audit-logs" onClick={() => setMenuOpen(false)}>Audit Logs</Link>
                <Link to="/security-dashboard" onClick={() => setMenuOpen(false)}>Security Dashboard</Link>
                <Link to="/reports" onClick={() => setMenuOpen(false)}>Reports</Link>
              </div>
            )}
          </div>
          <button className="logout-btn" onClick={handleLogout}>Log out</button>
        </div>
      )}
    </header>
  );
}
