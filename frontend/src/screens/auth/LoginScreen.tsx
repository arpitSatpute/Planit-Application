import React, { useState } from 'react';
import { View, Text, StyleSheet, KeyboardAvoidingView, Platform } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '../../components/Button';
import { Input } from '../../components/Input';
import { theme } from '../../theme';
import apiClient from '../../api/client';
import { useAuthStore } from '../../store/authStore';
import { toast } from '../../utils/toast';

export const LoginScreen = ({ navigation }: any) => {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const login = useAuthStore((state) => state.login);

  const handleLogin = async () => {
    if (!identifier.trim() || !password.trim()) {
      toast.error('Missing fields', 'Please enter your email/phone and password.');
      return;
    }

    setIsLoading(true);
    try {
      console.log('[Login] POST /auth/login →', { identifier: identifier.trim() });

      // Backend LoginRequest expects: { identifier, password }
      const response = await apiClient.post('/auth/login', {
        identifier: identifier.trim(),
        password,
      });

      console.log('[Login] Response status:', response.status);
      console.log('[Login] Response body:', JSON.stringify(response.data));

      // Backend wraps all responses in ApiResponse<T>: { success, data, message }
      const payload = response.data?.data;
      const accessToken = payload?.accessToken;
      const userSummary = payload?.user;

      if (accessToken && userSummary) {
        await login(
          {
            id: userSummary.id,
            firstName: userSummary.firstName,
            lastName: userSummary.lastName,
            email: userSummary.email,
            role: userSummary.role,
          },
          accessToken
        );
        toast.success('Welcome back!', `Hello, ${userSummary.firstName} 👋`);
        // AppNavigator detects token in store and auto-switches to MainFlow
      } else {
        console.warn('[Login] Unexpected payload:', payload);
        toast.error('Login failed', response.data?.message || 'Unexpected response. Please try again.');
      }
    } catch (error: any) {
      console.error('[Login] Error:', error?.response?.status, error?.response?.data ?? error?.message);
      const msg =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        (error?.code === 'ECONNREFUSED' || error?.message?.includes('Network')
          ? 'Cannot reach server. Is the backend running on port 8080?'
          : 'Invalid credentials. Please check and try again.');
      toast.error('Login failed', msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.container}
      >
        <View style={styles.header}>
          <Text style={styles.title}>Welcome back</Text>
          <Text style={styles.subtitle}>Sign in with your email or phone number.</Text>
        </View>

        <View style={styles.form}>
          <Input
            label="Email or Phone"
            placeholder="e.g. john@email.com or +919876543210"
            keyboardType="email-address"
            autoCapitalize="none"
            autoCorrect={false}
            value={identifier}
            onChangeText={setIdentifier}
          />
          <Input
            label="Password"
            placeholder="Enter your password"
            secureTextEntry
            value={password}
            onChangeText={setPassword}
          />
        </View>

        <View style={styles.footer}>
          <Button
            title="Sign In"
            onPress={handleLogin}
            isLoading={isLoading}
          />
          <View style={styles.registerContainer}>
            <Text style={styles.registerText}>Don't have an account? </Text>
            <Button title="Sign up" variant="text" onPress={() => navigation.navigate('Register')} />
          </View>
        </View>

        <View style={styles.debugContainer}>
          <Text style={styles.debugText}>Backend: http://localhost:8085/api/v1</Text>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: theme.colors.background },
  container: { flex: 1, paddingHorizontal: theme.spacing.lg, justifyContent: 'center' },
  header: { marginBottom: theme.spacing.xl },
  title: { fontSize: 28, fontWeight: '700', color: theme.colors.primary, marginBottom: theme.spacing.xs },
  subtitle: { fontSize: 16, color: theme.colors.textMuted },
  form: { marginBottom: theme.spacing.xl },
  footer: { marginTop: theme.spacing.md },
  registerContainer: { flexDirection: 'row', justifyContent: 'center', alignItems: 'center', marginTop: theme.spacing.lg },
  registerText: { color: theme.colors.textMuted, fontSize: 14 },
  debugContainer: { marginTop: theme.spacing.xl, alignItems: 'center', opacity: 0.5 },
  debugText: { fontSize: 11, color: theme.colors.textMuted, fontFamily: Platform.OS === 'ios' ? 'Courier' : 'monospace' },
});
