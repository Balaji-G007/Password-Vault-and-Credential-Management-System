import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getUnreadCount, fetchNotifications, markAllAsRead } from '../api/notificationService';

const TYPE_ICONS = {
  LOGIN_SUCCESS: '🔐',
  FAILED_LOGIN_ALERT: '🚨',
  CREDENTIAL_SHARED: '🔗',
  PASSWORD_EXPIRING: '🔑',
  SUSPICIOUS_RISK: '⚠️',
};

function formatTimestamp(dateStr) {
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now - date;
  const diffMin = Math.floor(diffMs / 60000);
  const diffHrs = Math.floor(diffMin / 60);

  if (diffMin < 1) return 'Just now';
  if (diffMin < 60) return `${diffMin}m ago`;
  if (diffHrs < 24) return `${diffHrs}h ago`;

  const isToday = date.toDateString() === now.toDateString();
  const time = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  if (isToday) return `Today at ${time}`;

  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) return `Yesterday at ${time}`;

  return date.toLocaleDateString([], { month: 'short', day: 'numeric' }) + ` ${time}`;
}

export default function NotificationBell() {
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  const refreshCount = useCallback(async () => {
    try {
      const res = await getUnreadCount();
      setUnreadCount(typeof res.data === 'number' ? res.data : res.data?.count ?? 0);
    } catch {
      /* silently fail — will retry on next poll */
    }
  }, []);

  const loadRecent = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetchNotifications();
      const list = Array.isArray(res.data) ? res.data : res.data?.notifications ?? [];
      setNotifications(list.slice(0, 5));
    } catch {
      /* silently fail */
    } finally {
      setLoading(false);
    }
  }, []);

  /* Poll unread count every 30s + on mount */
  useEffect(() => {
    refreshCount();
    const id = setInterval(refreshCount, 30000);
    return () => clearInterval(id);
  }, [refreshCount]);

  /* Load recent when dropdown opens */
  useEffect(() => {
    if (open) loadRecent();
  }, [open, loadRecent]);

  /* Close on outside click */
  useEffect(() => {
    if (!open) return;
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [open]);

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch {
      /* silently fail */
    }
  };

  const goToNotifications = () => {
    setOpen(false);
    navigate('/notifications');
  };

  return (
    <div className="notif-bell-wrap" ref={dropdownRef}>
      <button
        className="notif-bell-btn"
        onClick={() => setOpen((v) => !v)}
        aria-label="Notifications"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 01-3.46 0" />
        </svg>
        {unreadCount > 0 && (
          <span className="notif-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
        )}
      </button>

      {open && (
        <div className="notif-dropdown">
          <div className="notif-dropdown-header">
            <span className="notif-dropdown-title">Notifications</span>
            {unreadCount > 0 && (
              <button className="notif-mark-all" onClick={handleMarkAllRead}>
                Mark all read
              </button>
            )}
          </div>

          <div className="notif-dropdown-list">
            {loading && <div className="notif-empty">Loading…</div>}
            {!loading && notifications.length === 0 && (
              <div className="notif-empty">No notifications yet</div>
            )}
            {!loading && notifications.map((n) => (
              <div
                key={n.id}
                className={`notif-item ${n.read ? '' : 'unread'}`}
                onClick={goToNotifications}
              >
                <span className="notif-icon">{TYPE_ICONS[n.type] || '🔔'}</span>
                <div className="notif-body">
                  <p className="notif-msg">{n.message || n.title || n.type}</p>
                  <span className="notif-time">{formatTimestamp(n.createdAt || n.timestamp)}</span>
                </div>
              </div>
            ))}
          </div>

          <button className="notif-view-all" onClick={goToNotifications}>
            View all notifications →
          </button>
        </div>
      )}
    </div>
  );
}
