import Toast from 'react-native-toast-message';

/**
 * Centralised toast helper so every screen uses consistent styles.
 */
export const toast = {
  success: (title: string, message?: string) => {
    Toast.show({ type: 'success', text1: title, text2: message, visibilityTime: 3000 });
  },
  error: (title: string, message?: string) => {
    Toast.show({ type: 'error', text1: title, text2: message, visibilityTime: 4000 });
  },
  info: (title: string, message?: string) => {
    Toast.show({ type: 'info', text1: title, text2: message, visibilityTime: 3000 });
  },
  /** Extract a readable error message from an Axios error or plain Error. */
  fromError: (err: any, fallback = 'Something went wrong. Please try again.') => {
    const msg =
      err?.response?.data?.message ||
      err?.response?.data?.error ||
      err?.message ||
      fallback;
    Toast.show({ type: 'error', text1: 'Error', text2: msg, visibilityTime: 4000 });
  },
};
