import { createContext, useContext, useState, useCallback } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('vaultkeep_user');
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback((token, userData) => {
    localStorage.setItem('vaultkeep_token', token);
    localStorage.setItem('vaultkeep_user', JSON.stringify(userData));
    setUser(userData);
  }, []);

  const updateUser = useCallback((userData) => {
    localStorage.setItem('vaultkeep_user', JSON.stringify(userData));
    setUser(userData);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('vaultkeep_token');
    localStorage.removeItem('vaultkeep_user');
    setUser(null);
  }, []);

  const isAuthenticated = Boolean(localStorage.getItem('vaultkeep_token'));

  return (
    <AuthContext.Provider value={{ user, login, logout, updateUser, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
