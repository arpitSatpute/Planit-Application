import Constants from 'expo-constants';

// Get the API URL from app.json extra config
// If not available, fallback to the development IP or production URL
const getBaseUrl = (): string => {
  if (Constants.expoConfig?.extra?.backendApi) {
    return Constants.expoConfig.extra.backendApi;
  }
  
  if (!__DEV__) {
    // Production URL fallback
    return 'https://api.yourplanitapp.com/api/v1'; 
  }

  // General fallback
  return 'http://192.168.1.5:8080/api/v1';
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
