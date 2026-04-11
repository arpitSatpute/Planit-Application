import React, { useState } from 'react';
import { View, Text, StyleSheet, SafeAreaView, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { theme } from '../../theme';
import { useQuery } from '@tanstack/react-query';
import apiClient from '../../api/client';
import { useAuthStore } from '../../store/authStore';

export const BookingsScreen = () => {
  const [activeTab, setActiveTab] = useState('Upcoming');
  const user = useAuthStore(state => state.user);

  // Dynamic API based on role
  const isVendor = user?.role === 'VENDOR';
  const queryEndpoint = isVendor ? '/bookings/vendor' : '/bookings';

  const { data: bookings, isPending, isError, refetch } = useQuery({
    queryKey: ['bookings', queryEndpoint],
    queryFn: () => apiClient.get(queryEndpoint).then(res => res.data.data || []),
  });

  const getStatusColor = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'CONFIRMED': return theme.colors.success;
      case 'PENDING': return '#F59E0B'; // Amber
      case 'COMPLETED': return theme.colors.textMuted;
      case 'CANCELED':
      case 'CANCELLED': return theme.colors.error;
      default: return theme.colors.primary;
    }
  };

  const renderBookingCard = ({ item }: any) => (
    <View style={styles.card}>
      <View style={styles.cardHeader}>
        <Text style={styles.cardTitle}>{item.product?.name || item.title || 'Event Booking'}</Text>
        <View style={[styles.statusBadge, { backgroundColor: getStatusColor(item.status) + '20' }]}>
          <Text style={[styles.statusText, { color: getStatusColor(item.status) }]}>{item.status}</Text>
        </View>
      </View>

      <View style={styles.cardDetails}>
        <View style={styles.detailRow}>
          <Ionicons name="calendar-outline" size={16} color={theme.colors.textMuted} />
          {/* Format date gracefully or fallback */}
          <Text style={styles.detailText}>{item.date || item.startDate || 'TBD'}</Text>
        </View>
        <View style={styles.detailRow}>
          <Ionicons name="time-outline" size={16} color={theme.colors.textMuted} />
          <Text style={styles.detailText}>{item.time || 'TBD'}</Text>
        </View>
      </View>

      <View style={styles.cardFooter}>
        <Text style={styles.price}>{item.totalPrice ? `$${item.totalPrice}` : item.price || 'N/A'}</Text>
        <TouchableOpacity style={styles.actionBtn}>
          <Text style={styles.actionBtnText}>View Details</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  // Fallback to empty array safely
  const safeBookings = Array.isArray(bookings) ? bookings : [];
  
  // Filter logic simulating backend sorting
  const filteredBookings = activeTab === 'Upcoming' 
    ? safeBookings.filter(b => b.status !== 'COMPLETED' && b.status !== 'CANCELLED')
    : activeTab === 'Cancelled'
      ? safeBookings.filter(b => b.status === 'CANCELLED' || b.status === 'CANCELED')
      : safeBookings.filter(b => b.status === 'COMPLETED');

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>{isVendor ? 'Customer Bookings' : 'My Bookings'}</Text>
      </View>

      <View style={styles.tabsContainer}>
        {['Upcoming', 'Past', 'Cancelled'].map((tab) => (
          <TouchableOpacity 
            key={tab} 
            style={[styles.tab, activeTab === tab && styles.activeTab]}
            onPress={() => setActiveTab(tab)}
          >
            <Text style={[styles.tabText, activeTab === tab && styles.activeTabText]}>{tab}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {isPending ? (
        <View style={styles.centerContents}>
          <ActivityIndicator size="large" color={theme.colors.primary} />
        </View>
      ) : isError ? (
        <View style={styles.centerContents}>
          <Text style={styles.errorText}>Failed to load bookings.</Text>
          <TouchableOpacity onPress={() => refetch()}><Text style={styles.retryText}>Retry</Text></TouchableOpacity>
        </View>
      ) : filteredBookings.length === 0 ? (
        <View style={styles.centerContents}>
          <Text style={styles.emptyText}>No {activeTab.toLowerCase()} bookings found.</Text>
        </View>
      ) : (
        <FlatList
          data={filteredBookings}
          keyExtractor={(item) => item.id || Math.random().toString()}
          renderItem={renderBookingCard}
          contentContainerStyle={styles.listContainer}
          showsVerticalScrollIndicator={false}
          refreshing={isPending}
          onRefresh={refetch}
        />
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: theme.colors.background },
  header: { paddingHorizontal: theme.spacing.lg, paddingVertical: theme.spacing.md, backgroundColor: theme.colors.surface, borderBottomWidth: 1, borderBottomColor: theme.colors.border },
  headerTitle: { fontSize: 24, fontWeight: '700', color: theme.colors.primary },
  tabsContainer: { flexDirection: 'row', paddingHorizontal: theme.spacing.lg, paddingTop: theme.spacing.md, paddingBottom: theme.spacing.sm },
  tab: { marginRight: theme.spacing.lg, paddingBottom: theme.spacing.xs },
  activeTab: { borderBottomWidth: 2, borderBottomColor: theme.colors.primary },
  tabText: { fontSize: 16, color: theme.colors.textMuted, fontWeight: '500' },
  activeTabText: { color: theme.colors.primary, fontWeight: '600' },
  listContainer: { padding: theme.spacing.lg },
  centerContents: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  errorText: { color: theme.colors.error, marginBottom: theme.spacing.md },
  retryText: { color: theme.colors.accent, fontWeight: '600' },
  emptyText: { color: theme.colors.textMuted, fontSize: 16 },
  card: { backgroundColor: theme.colors.surface, borderRadius: theme.border.radius.lg, padding: theme.spacing.lg, marginBottom: theme.spacing.md, ...theme.shadows.sm },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: theme.spacing.md },
  cardTitle: { fontSize: 18, fontWeight: '600', color: theme.colors.text, flex: 1, marginRight: theme.spacing.md },
  statusBadge: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 4 },
  statusText: { fontSize: 10, fontWeight: '700', letterSpacing: 0.5 },
  cardDetails: { marginBottom: theme.spacing.md },
  detailRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 6 },
  detailText: { fontSize: 14, color: theme.colors.textMuted, marginLeft: 8 },
  cardFooter: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: theme.spacing.sm, paddingTop: theme.spacing.md, borderTopWidth: 1, borderTopColor: theme.colors.border },
  price: { fontSize: 18, fontWeight: '700', color: theme.colors.primary },
  actionBtn: { backgroundColor: theme.colors.background, paddingHorizontal: 16, paddingVertical: 8, borderRadius: theme.border.radius.sm },
  actionBtnText: { color: theme.colors.primary, fontSize: 14, fontWeight: '600' },
});
