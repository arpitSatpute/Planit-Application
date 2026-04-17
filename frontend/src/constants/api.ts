import Constants from 'expo-constants';

const normalizeUrl = (value: string): string => value.replace(/\/+$/, '');

const getBaseUrl = (): string => {
  const fromEnv = process.env.EXPO_PUBLIC_BACKEND_API;
  if (fromEnv) {
    return normalizeUrl(fromEnv);
  }

  if (__DEV__) {
    return 'http://localhost:8085/api/v1';
  }

  if (Constants.expoConfig?.extra?.backendApi) {
    return normalizeUrl(Constants.expoConfig.extra.backendApi);
  }

  return 'https://api.yourplanitapp.com/api/v1';
};

export const API_BASE_URL = getBaseUrl();

export const Endpoints = {
  // Auth
  register: '/auth/register',
  login: '/auth/login',
  refreshToken: '/auth/refresh-token',
  sendOtp: '/auth/send-otp',
  verifyOtp: '/auth/verify-otp',
  logout: '/auth/logout',

  // Products
  products: '/products',
  productById: (id: string) => `/products/${id}`,
  productBySlug: (slug: string) => `/products/slug/${slug}`,

  // Bookings
  bookings: '/bookings',
  bookingById: (id: string) => `/bookings/${id}`,
  vendorBookings: '/bookings/vendor',
  cancelBooking: (id: string) => `/bookings/${id}/cancel`,

  // Payments
  createPaymentOrder: '/payments/create-order',
  verifyPayment: '/payments/verify',

  // Reviews
  reviews: '/reviews',
  productReviews: (productId: string) => `/reviews/product/${productId}`,
  vendorResponse: (reviewId: string) => `/reviews/${reviewId}/vendor-response`,

  // Users
  myProfile: '/users/me',
  updateProfile: '/users/me/profile',
  updateAddress: '/users/me/address',
  updatePreferences: '/users/me/preferences',

  // Vendors
  vendorRegister: '/vendors/register',
  vendorById: (id: string) => `/vendors/${id}`,
  myVendor: '/vendors/me',

  // Chat
  sendMessage: '/chat/send',
  conversations: '/chat/conversations',
  conversationMessages: (id: string) => `/chat/conversations/${id}/messages`,
  markRead: (id: string) => `/chat/conversations/${id}/read`,

  // Admin
  adminUsers: '/admin/users',
  suspendUser: (id: string) => `/admin/users/${id}/suspend`,
  activateUser: (id: string) => `/admin/users/${id}/activate`,
  adminVendors: '/admin/vendors',
  verifyVendor: (id: string) => `/admin/vendors/${id}/verify`,
};
