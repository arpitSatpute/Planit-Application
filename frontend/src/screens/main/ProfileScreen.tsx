import React from 'react';
import { View, Text, StyleSheet, SafeAreaView, ScrollView, TouchableOpacity, Alert } from 'react-native';
import { toast } from '../../utils/toast';
import { Ionicons } from '@expo/vector-icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { theme } from '../../theme';
import { useAuthStore } from '../../store/authStore';
import apiClient from '../../api/client';

export const ProfileScreen = () => {
  const logout = useAuthStore((state) => state.logout);
  const user = useAuthStore((state) => state.user);
  const setUser = useAuthStore((state) => state.setUser);
  const queryClient = useQueryClient();

  const { data: profileData, refetch } = useQuery({
    queryKey: ['my-profile'],
    queryFn: () => apiClient.get('/users/me').then(res => res.data?.data),
  });

  const updateProfileMutation = useMutation({
    mutationFn: async () => {
      const firstName = profileData?.profile?.firstName || user?.firstName || 'Demo';
      const lastName = profileData?.profile?.lastName || user?.lastName || 'User';
      return apiClient.put('/users/me/profile', {
        firstName,
        lastName,
        avatar: profileData?.profile?.avatar || null,
        gender: profileData?.profile?.gender || null,
        dateOfBirth: profileData?.profile?.dateOfBirth || null,
      });
    },
    onSuccess: async () => {
      await refetch();
      queryClient.invalidateQueries({ queryKey: ['my-profile'] });
      toast.success('Profile synced', 'Your profile has been updated.');
    },
    onError: (error: any) => {
      toast.error('Update failed', error?.response?.data?.message || 'Could not update profile right now.');
    }
  });

  React.useEffect(() => {
    if (!profileData) return;
    if (!user) return;

    const nextUser = {
      ...user,
      firstName: profileData?.profile?.firstName || user.firstName,
      lastName: profileData?.profile?.lastName || user.lastName,
      email: profileData?.email || user.email,
      role: (profileData?.role || user.role) as any,
    };
    setUser(nextUser);
  }, [profileData]);

  const handleLogout = () => {
    Alert.alert('Log Out', 'Are you sure you want to log out?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Log Out',
        style: 'destructive',
        onPress: async () => {
          await logout();
          toast.info('Logged out', 'See you soon!');
        },
      },
    ]);
  };

  const renderMenuItem = (icon: string, title: string, subtitle?: string, isDestructive = false) => (
    <TouchableOpacity style={styles.menuItem} activeOpacity={0.7}>
      <View style={[styles.iconContainer, isDestructive && styles.iconContainerDestructive]}>
        <Ionicons name={icon as any} size={22} color={isDestructive ? theme.colors.error : theme.colors.primary} />
      </View>
      <View style={styles.menuContent}>
        <Text style={[styles.menuTitle, isDestructive && styles.menuTitleDestructive]}>{title}</Text>
        {subtitle && <Text style={styles.menuSubtitle}>{subtitle}</Text>}
      </View>
      <Ionicons name="chevron-forward" size={20} color={theme.colors.border} />
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView showsVerticalScrollIndicator={false}>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Profile</Text>
        </View>

        <View style={styles.profileSection}>
          <View style={styles.avatarLarge}>
            <Ionicons name="person" size={40} color={theme.colors.textMuted} />
          </View>
          <Text style={styles.profileName}>
            {profileData?.profile?.firstName || user?.firstName} {profileData?.profile?.lastName || user?.lastName}
          </Text>
          <Text style={styles.profileEmail}>{profileData?.email || user?.email}</Text>
          <TouchableOpacity style={styles.editProfileBtn} onPress={() => updateProfileMutation.mutate()}>
            <Text style={styles.editProfileText}>
              {updateProfileMutation.isPending ? 'Updating...' : 'Update Profile'}
            </Text>
          </TouchableOpacity>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Account Settings</Text>
          <View style={styles.menuCard}>
            {renderMenuItem('person-circle-outline', 'Personal Information', 'Update your name, email and phone')}
            <View style={styles.separator} />
            {renderMenuItem('card-outline', 'Payment Methods', 'Manage your cards and bank accounts')}
            <View style={styles.separator} />
            {renderMenuItem('notifications-outline', 'Notifications', 'Control push and email alerts')}
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Hosting</Text>
          <View style={styles.menuCard}>
            {renderMenuItem('business-outline', 'Switch to Vendor', 'Start earning by hosting spaces')}
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Support & About</Text>
          <View style={styles.menuCard}>
            {renderMenuItem('help-circle-outline', 'Help Center', 'FAQs and contact support')}
            <View style={styles.separator} />
            {renderMenuItem('document-text-outline', 'Terms of Service')}
          </View>
        </View>

        <View style={styles.logoutSection}>
          <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout}>
            <Text style={styles.logoutText}>Log Out</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  header: {
    paddingHorizontal: theme.spacing.lg,
    paddingVertical: theme.spacing.md,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  profileSection: {
    alignItems: 'center',
    paddingVertical: theme.spacing.lg,
    marginBottom: theme.spacing.sm,
  },
  avatarLarge: {
    width: 96,
    height: 96,
    borderRadius: 48,
    backgroundColor: theme.colors.surface,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: theme.colors.border,
    marginBottom: theme.spacing.md,
    ...theme.shadows.sm,
  },
  profileName: {
    fontSize: 22,
    fontWeight: '700',
    color: theme.colors.text,
    marginBottom: 4,
  },
  profileEmail: {
    fontSize: 14,
    color: theme.colors.textMuted,
    marginBottom: theme.spacing.md,
  },
  editProfileBtn: {
    paddingHorizontal: theme.spacing.lg,
    paddingVertical: 8,
    borderRadius: theme.border.radius.xl,
    backgroundColor: theme.colors.surface,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  editProfileText: {
    fontSize: 14,
    fontWeight: '600',
    color: theme.colors.primary,
  },
  section: {
    paddingHorizontal: theme.spacing.lg,
    marginBottom: theme.spacing.xl,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: theme.colors.textMuted,
    marginBottom: theme.spacing.sm,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  menuCard: {
    backgroundColor: theme.colors.surface,
    borderRadius: theme.border.radius.lg,
    overflow: 'hidden',
    ...theme.shadows.sm,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: theme.spacing.md,
    backgroundColor: theme.colors.surface,
  },
  iconContainer: {
    width: 40,
    height: 40,
    borderRadius: 8,
    backgroundColor: theme.colors.background,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: theme.spacing.md,
  },
  iconContainerDestructive: {
    backgroundColor: theme.colors.error + '10',
  },
  menuContent: {
    flex: 1,
    justifyContent: 'center',
  },
  menuTitle: {
    fontSize: 16,
    fontWeight: '500',
    color: theme.colors.text,
  },
  menuTitleDestructive: {
    color: theme.colors.error,
  },
  menuSubtitle: {
    fontSize: 13,
    color: theme.colors.textMuted,
    marginTop: 2,
  },
  separator: {
    height: 1,
    backgroundColor: theme.colors.border,
    marginLeft: 56 + theme.spacing.md,
  },
  logoutSection: {
    paddingHorizontal: theme.spacing.lg,
    paddingBottom: theme.spacing.xxl,
    marginTop: theme.spacing.md,
  },
  logoutBtn: {
    backgroundColor: theme.colors.surface,
    borderRadius: theme.border.radius.md,
    paddingVertical: theme.spacing.md,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: theme.colors.error + '50',
  },
  logoutText: {
    fontSize: 16,
    fontWeight: '600',
    color: theme.colors.error,
  },
});
