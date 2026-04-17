import React, { useState } from 'react';
import { View, Text, StyleSheet, KeyboardAvoidingView, Platform, SafeAreaView, ScrollView, TouchableOpacity } from 'react-native';
import { Button } from '../../components/Button';
import { Input } from '../../components/Input';
import { theme } from '../../theme';
import apiClient from '../../api/client';
import { useAuthStore } from '../../store/authStore';
import { toast } from '../../utils/toast';

export const RegisterScreen = ({ navigation }: any) => {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<'USER' | 'VENDOR'>('USER'); // Role based entry logic
  const [isLoading, setIsLoading] = useState(false);

  const login = useAuthStore((state) => state.login);

  const handleRegister = async () => {
    if (!firstName || !lastName || !email || !phone || !password) {
      toast.error('Validation Error', 'Please fill in all required fields.');
      return;
    }

    if (!/^\+[1-9]\d{1,14}$/.test(phone.trim())) {
      toast.error('Invalid Phone', 'Phone must be in E.164 format, e.g. +919876543210');
      return;
    }

    setIsLoading(true);
    try {
      const payload = {
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim().toLowerCase(),
        phone: phone.trim(),
        password,
        role
      };

      console.log('[Register] POST /auth/register →', { ...payload, password: '***' });
      const response = await apiClient.post('/auth/register', payload);
      console.log('[Register] Response status:', response.status);
      console.log('[Register] Response body:', JSON.stringify(response.data));

      // Backend wraps response in ApiResponse: { success, data, message }
      const data = response.data.data;
      const accessToken = data?.accessToken;
      const userSummary = data?.user;

      if (accessToken && userSummary) {
        await login(
          {
            id: userSummary.id,
            firstName: userSummary.firstName,
            lastName: userSummary.lastName,
            email: userSummary.email,
            role: userSummary.role as any,
          },
          accessToken
        );
        toast.success('Account created!', `Welcome aboard, ${userSummary.firstName} 🎉`);
      } else {
        toast.success('Registered!', 'Account created. Please sign in.');
        navigation.navigate('Login');
      }

    } catch (error: any) {
      console.error('[Register] Error:', error?.response?.status, error?.response?.data ?? error?.message);
      const msg =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        (error?.code === 'ECONNREFUSED' || error?.message?.includes('Network')
          ? 'Cannot reach server. Is the backend running on port 8080?'
          : 'Registration failed. Please try again.');
      toast.error('Registration failed', msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={styles.keyboardView}>
        <ScrollView contentContainerStyle={styles.container} showsVerticalScrollIndicator={false}>
          <View style={styles.header}>
            <Text style={styles.title}>Create Account</Text>
            <Text style={styles.subtitle}>Join our platform to book the best venues and services.</Text>
          </View>

          <View style={styles.roleContainer}>
            <TouchableOpacity
              style={[styles.roleBtn, role === 'USER' && styles.roleBtnActive]}
              onPress={() => setRole('USER')}
            >
              <Text style={[styles.roleText, role === 'USER' && styles.roleTextActive]}>I'm a Planner</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.roleBtn, role === 'VENDOR' && styles.roleBtnActive]}
              onPress={() => setRole('VENDOR')}
            >
              <Text style={[styles.roleText, role === 'VENDOR' && styles.roleTextActive]}>I'm a Vendor</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.form}>
            <View style={styles.row}>
              <Input label="First Name" placeholder="John" value={firstName} onChangeText={setFirstName} containerStyle={[styles.flex1, { marginRight: theme.spacing.sm }]} />
              <Input label="Last Name" placeholder="Doe" value={lastName} onChangeText={setLastName} containerStyle={styles.flex1} />
            </View>
            <Input label="Email" placeholder="Enter your email" keyboardType="email-address" autoCapitalize="none" value={email} onChangeText={setEmail} />
            <Input label="Phone Number" placeholder="+1 234 567 8900" keyboardType="phone-pad" value={phone} onChangeText={setPhone} />
            <Input label="Password" placeholder="Create a secure password" secureTextEntry value={password} onChangeText={setPassword} />
          </View>

          <View style={styles.footer}>
            <Button title="Sign Up" onPress={handleRegister} isLoading={isLoading} />
            <View style={styles.loginContainer}>
              <Text style={styles.loginText}>Already have an account? </Text>
              <Button title="Log In" variant="text" onPress={() => navigation.navigate('Login')} />
            </View>
            <View style={styles.debugContainer}>
              <Text style={styles.debugText}>Backend: http://localhost:8085/api/v1</Text>
            </View>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: theme.colors.background },
  keyboardView: { flex: 1 },
  container: { flexGrow: 1, paddingHorizontal: theme.spacing.lg, justifyContent: 'center', paddingVertical: theme.spacing.xl },
  header: { marginBottom: theme.spacing.xl },
  title: { fontSize: 28, fontWeight: '700', color: theme.colors.primary, marginBottom: theme.spacing.xs },
  subtitle: { fontSize: 16, color: theme.colors.textMuted },
  roleContainer: { flexDirection: 'row', marginBottom: theme.spacing.xl, backgroundColor: theme.colors.surface, borderRadius: theme.border.radius.md, padding: 4, borderWidth: 1, borderColor: theme.colors.border },
  roleBtn: { flex: 1, paddingVertical: 12, alignItems: 'center', borderRadius: theme.border.radius.sm },
  roleBtnActive: { backgroundColor: theme.colors.primary },
  roleText: { fontSize: 14, fontWeight: '600', color: theme.colors.textMuted },
  roleTextActive: { color: theme.colors.surface },
  form: { marginBottom: theme.spacing.xl },
  row: { flexDirection: 'row' },
  flex1: { flex: 1 },
  footer: { marginTop: 'auto' },
  loginContainer: { flexDirection: 'row', justifyContent: 'center', alignItems: 'center', marginTop: theme.spacing.md },
  loginText: { color: theme.colors.textMuted, fontSize: 14 },
  debugContainer: { marginTop: theme.spacing.lg, alignItems: 'center', opacity: 0.5 },
  debugText: { fontSize: 11, color: theme.colors.textMuted, fontFamily: Platform.OS === 'ios' ? 'Courier' : 'monospace' },
});
