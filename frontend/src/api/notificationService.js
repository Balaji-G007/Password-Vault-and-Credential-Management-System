import api from './client';

export function fetchNotifications() {
  return api.get('/api/notifications');
}

export function getUnreadCount() {
  return api.get('/api/notifications/unread-count');
}

export function markAsRead(id) {
  return api.patch(`/api/notifications/${id}/read`);
}

export function markAllAsRead() {
  return api.patch('/api/notifications/read-all');
}
