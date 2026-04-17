import axios from 'axios';
import { API_BASE_URL } from '../constants/api';
import { storage } from '../utils/storage';
import { useAuthStore } from '../store/authStore';

console.log('[API] Base URL:', API_BASE_URL);

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Request Interceptor — attach token and log every outbound request
apiClient.interceptors.request.use(
  async (config) => {
    const token = await storage.getItem('auth_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log(`[API →] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
    return config;
  },
  (error) => {
    console.error('[API request error]', error);
    return Promise.reject(error);
  }
);

// Response Interceptor — log responses and handle 401 globally
apiClient.interceptors.response.use(
  (response) => {
    console.log(`[API ←] ${response.status} ${response.config.url}`);
    return response;
  },
  async (error) => {
    const status = error?.response?.status;
    const url = error?.config?.url ?? '';
    console.error(`[API ✗] ${status ?? 'NETWORK_ERR'} ${url}`, error?.response?.data ?? error?.message);

    // Force logout on 401 unauthorized
    if (status === 401) {
      useAuthStore.getState().logout();
    }
    return Promise.reject(error);
  }
);

export default apiClient;
