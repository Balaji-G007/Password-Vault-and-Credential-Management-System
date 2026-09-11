import axios from 'axios';

// In local dev (npm run dev), talk directly to the backend on 8081.
// In production (built + served via Nginx), use an empty base so relative
// /api/... calls go through Nginx's reverse proxy on the same origin.
// In local dev (VITE_API_URL not set), talk to backend on 8081.
// In production, .env.production has VITE_API_URL="" → empty string = relative /api calls via Nginx.
const envUrl = import.meta.env.VITE_API_URL;
export const API_BASE = envUrl !== undefined ? envUrl : 'http://localhost:8081';

const api = axios.create({ baseURL: API_BASE });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('vaultkeep_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('vaultkeep_token');
      localStorage.removeItem('vaultkeep_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;