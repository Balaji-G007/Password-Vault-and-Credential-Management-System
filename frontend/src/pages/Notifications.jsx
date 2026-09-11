import { useState, useEffect, useCallback } from 'react';
import { fetchNotifications, markAsRead, markAllAsRead, getUnreadCount } from '../api/notificationService';
import AppHeader from '../components/AppHeader';

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
  if (diffMin < 60) return `${diffMin} minutes ago`;
  if (diffHrs < 24) return `${diffHrs} hours ago`;

  const isToday = date.toDateString() === now.toDateString();
  const time = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  if (isToday) return `Today at ${time}`;

  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) return `Yesterday at ${time}`;

  return date.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' }) + ` at ${time}`;
}

export default function Notifications() {
  const [notifications, setNotifications] = useState(null);
  const [filter, setFilter] = useState('all');
  const [unreadCount, setUnreadCount] = useState(0);
  const [loadError, setLoadError] = useState('');

  const loadData = useCallback(async () => {
    try {
      const [notifRes, countRes] = await Promise.all([
        fetchNotifications(),
        getUnreadCount(),
      ]);
      const list = Array.isArray(notifRes.data) ? notifRes.data : notifRes.data?.notifications ?? [];
      setNotifications(list);
      setUnreadCount(typeof countRes.data === 'number' ? countRes.data : countRes.data?.count ?? 0);
      setLoadError('');
    } catch {
      setLoadError('Could not load notifications.');
    }
  }, []);

  useEffect(() => { loadData(); }, [loadData]);

  const handleMarkRead = async (id) => {
    try {
      await markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch {
      /* silently fail */
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch {
      /* silently fail */
    }
  };

  const filtered = notifications
    ? filter === 'unread'
      ? notifications.filter((n) => !n.read)
      : notifications
    : [];

  return (
    <>
      <AppHeader variant="full" />
      <main className="page">
        <h1 className="page-title">
          Notifications
          {unreadCount > 0 && <span className="notif-count-title">{unreadCount} unread</span>}
        </h1>
        <p className="page-subtitle">Stay informed about your account activity and security.</p>

        <div className="notif-toolbar">
          <div className="notif-tabs">
            <button
              className={`notif-tab ${filter === 'all' ? 'active' : ''}`}
              onClick={() => setFilter('all')}
            >
              All
            </button>
            <button
              className={`notif-tab ${filter === 'unread' ? 'active' : ''}`}
              onClick={() => setFilter('unread')}
            >
              Unread
              {unreadCount > 0 && <span className="notif-tab-count">{unreadCount}</span>}
            </button>
          </div>
          {unreadCount > 0 && (
            <button className="save-btn" onClick={handleMarkAllRead}>
              Mark all as read
            </button>
          )}
        </div>

        {notifications === null && !loadError && (
          <div className="empty-state">Loading notifications…</div>
        )}
        {loadError && <div className="empty-state">{loadError}</div>}
        {notifications && filtered.length === 0 && (
          <div className="empty-state">
            {filter === 'unread' ? 'No unread notifications.' : 'No notifications yet.'}
          </div>
        )}
        {notifications && filtered.map((n) => (
          <div
            key={n.id}
            className={`notif-card ${n.read ? '' : 'unread'}`}
            onClick={() => !n.read && handleMarkRead(n.id)}
          >
            <span className="notif-card-icon">{TYPE_ICONS[n.type] || '🔔'}</span>
            <div className="notif-card-body">
              <div className="notif-card-top">
                <span className="notif-card-type">{n.type?.replace(/_/g, ' ') || 'Notification'}</span>
                <span className="notif-card-time">{formatTimestamp(n.createdAt || n.timestamp)}</span>
              </div>
              <p className="notif-card-msg">{n.message || n.title || n.type}</p>
            </div>
            {!n.read && <span className="notif-dot" />}
          </div>
        ))}
      </main>
    </>
  );
}
