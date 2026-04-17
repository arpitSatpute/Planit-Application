import { create } from 'zustand';
import { storage } from '../utils/storage';

export type Role = 'USER' | 'VENDOR' | 'ADMIN' | 'PLANNER';

interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
}

interface AuthState {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  login: (user: User, token: string) => Promise<void>;
  setUser: (user: User) => Promise<void>;
  logout: () => Promise<void>;
  hydrateAuth: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  isLoading: true, // starts loading until we hydrate
  
  login: async (user, token) => {
    await storage.setItem('auth_token', token);
    await storage.setItem('auth_user', JSON.stringify(user));
    set({ user, token });
  },

  setUser: async (user) => {
    await storage.setItem('auth_user', JSON.stringify(user));
    set({ user });
  },

  logout: async () => {
    await storage.deleteItem('auth_token');
    await storage.deleteItem('auth_user');
    set({ user: null, token: null });
  },

  hydrateAuth: async () => {
    try {
      const token = await storage.getItem('auth_token');
      const userStr = await storage.getItem('auth_user');
      
      if (token && userStr) {
        set({ user: JSON.parse(userStr), token, isLoading: false });
      } else {
        set({ user: null, token: null, isLoading: false });
      }
    } catch (error) {
      console.log('Failed to hydrate auth', error);
      set({ user: null, token: null, isLoading: false });
    }
  }
}));
