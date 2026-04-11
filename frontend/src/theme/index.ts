export const colors = {
  primary: '#1E293B', // Deep Slate / Navy
  primaryLight: '#334155',
  secondaryBase: '#0F172A',
  accent: '#3B82F6', // Blue for active states
  background: '#F8FAFC', // Very light grey blue background
  surface: '#FFFFFF', // White cards
  text: '#111827', // Almost black
  textMuted: '#64748B',
  border: '#E2E8F0',
  error: '#EF4444',
  success: '#10B981',
};

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

export const border = {
  radius: {
    sm: 6,
    md: 10,
    lg: 16,
    xl: 24,
  },
};

export const shadows = {
  sm: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  md: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 8,
    elevation: 4,
  },
};

export const theme = {
  colors,
  spacing,
  border,
  shadows,
};

export default theme;
