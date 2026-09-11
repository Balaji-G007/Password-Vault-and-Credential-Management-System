import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import Dashboard from './pages/Dashboard';
import Profile from './pages/Profile';
import AuditLogs from './pages/AuditLogs';
import SuspiciousActivity from './pages/SuspiciousActivity';
import SecurityAlerts from './pages/SecurityAlerts';
import LoginActivities from './pages/LoginActivities';
import SecurityDashboard from './pages/SecurityDashboard';
import Reports from './pages/Reports';
import Notifications from './pages/Notifications';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />

          <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
          <Route path="/audit-logs" element={<ProtectedRoute><AuditLogs /></ProtectedRoute>} />
          <Route path="/suspicious-activity" element={<ProtectedRoute><SuspiciousActivity /></ProtectedRoute>} />
          <Route path="/security-alerts" element={<ProtectedRoute><SecurityAlerts /></ProtectedRoute>} />
          <Route path="/login-activities" element={<ProtectedRoute><LoginActivities /></ProtectedRoute>} />
          <Route path="/security-dashboard" element={<ProtectedRoute><SecurityDashboard /></ProtectedRoute>} />
          <Route path="/reports" element={<ProtectedRoute><Reports /></ProtectedRoute>} />
          <Route path="/notifications" element={<ProtectedRoute><Notifications /></ProtectedRoute>} />

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
