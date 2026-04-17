import React from 'react';
import { View, Text, StyleSheet, ScrollView, SafeAreaView, TouchableOpacity, Image } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { Ionicons } from '@expo/vector-icons';
import { theme } from '../../theme';
import apiClient from '../../api/client';
import { useAuthStore } from '../../store/authStore';

export const HomeScreen = ({ navigation }: any) => {
  const user = useAuthStore(state => state.user);
  const { data: products, isPending } = useQuery({
    queryKey: ['products'],
    queryFn: () => apiClient.get('/products').then(res => res.data?.data || []),
  });

  const formatPrice = (value?: number) => {
    if (!value) return 'TBD';
    return `INR ${(value / 100).toLocaleString()}`;
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.container}>
        <View style={styles.header}>
          <View>
            <Text style={styles.greeting}>Good morning,</Text>
            <Text style={styles.name}>{user ? `${user.firstName} ${user.lastName}` : 'Guest'}</Text>
          </View>
          <TouchableOpacity style={styles.profileAvatar} onPress={() => navigation.navigate('Profile')}>
            <Ionicons name="person" size={20} color={theme.colors.textMuted} />
          </TouchableOpacity>
        </View>

        <View style={styles.searchBar}>
          <Ionicons name="search" size={20} color={theme.colors.textMuted} style={styles.searchIcon} />
          <Text style={styles.searchText}>Search events, vendors, or venues...</Text>
        </View>

        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Featured Spaces</Text>
            <Text style={styles.seeAll}>See All</Text>
          </View>
          
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.cardsScroll}>
            {isPending ? (
               <Text style={[styles.loadingText, {marginLeft: 20}]}>Loading featured...</Text>
            ) : products && products.length > 0 ? (
              products.slice(0, 3).map((item: any, index: number) => (
                <TouchableOpacity 
                  key={item.id || index.toString()} 
                  style={styles.card} 
                  activeOpacity={0.9}
                  onPress={() =>
                    navigation.navigate('ProductDetails', {
                      productId: item.id,
                      title: item.name,
                    })
                  }
                >
                  <View style={styles.cardImagePlaceholder}>
                    <Ionicons name="image-outline" size={40} color={theme.colors.border} />
                  </View>
                  <View style={styles.cardContent}>
                    <Text style={styles.cardTitle} numberOfLines={1}>{item.name}</Text>
                    <Text style={styles.cardSubtitle}>{item.location?.city || item.city || 'No Location'}</Text>
                    <Text style={styles.cardPrice}>{formatPrice(item.pricingModel?.basePrice)} / hr</Text>
                  </View>
                </TouchableOpacity>
              ))
            ) : (
                <Text style={{marginLeft: 20, color: theme.colors.textMuted}}>No spaces available.</Text>
            )}
          </ScrollView>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Recent Products from API</Text>
          {isPending ? (
            <Text style={styles.loadingText}>Fetching products...</Text>
          ) : products && products.length > 0 ? (
            products.map((product: any, index: number) => (
              <View key={index} style={styles.listItem}>
                <Text style={styles.listTitle}>{product.name || 'API Product Name'}</Text>
              </View>
            ))
          ) : (
            <View style={styles.emptyState}>
              <Text style={styles.emptyText}>No products found.</Text>
            </View>
          )}
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
  container: {
    padding: theme.spacing.lg,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: theme.spacing.xl,
    paddingTop: theme.spacing.md,
  },
  greeting: {
    fontSize: 14,
    color: theme.colors.textMuted,
    marginBottom: 4,
  },
  name: {
    fontSize: 24,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  profileAvatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: theme.colors.surface,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: theme.colors.surface,
    height: 52,
    borderRadius: theme.border.radius.lg,
    paddingHorizontal: theme.spacing.md,
    marginBottom: theme.spacing.xl,
    ...theme.shadows.sm,
  },
  searchIcon: {
    marginRight: theme.spacing.sm,
  },
  searchText: {
    color: theme.colors.textMuted,
    fontSize: 15,
  },
  section: {
    marginBottom: theme.spacing.xl,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: theme.spacing.md,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: theme.colors.primary,
    marginBottom: theme.spacing.md,
  },
  seeAll: {
    fontSize: 14,
    color: theme.colors.accent,
    fontWeight: '500',
  },
  cardsScroll: {
    marginHorizontal: -theme.spacing.lg,
    paddingHorizontal: theme.spacing.lg,
  },
  card: {
    width: 240,
    backgroundColor: theme.colors.surface,
    borderRadius: theme.border.radius.lg,
    marginRight: theme.spacing.md,
    overflow: 'hidden',
    ...theme.shadows.md,
  },
  cardImagePlaceholder: {
    height: 140,
    backgroundColor: theme.colors.background,
    justifyContent: 'center',
    alignItems: 'center',
  },
  cardContent: {
    padding: theme.spacing.md,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: theme.colors.text,
    marginBottom: 4,
  },
  cardSubtitle: {
    fontSize: 13,
    color: theme.colors.textMuted,
    marginBottom: 8,
  },
  cardPrice: {
    fontSize: 15,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  loadingText: {
    color: theme.colors.textMuted,
    fontStyle: 'italic',
  },
  emptyState: {
    backgroundColor: theme.colors.surface,
    padding: theme.spacing.xl,
    borderRadius: theme.border.radius.md,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderStyle: 'dashed',
  },
  emptyText: {
    color: theme.colors.textMuted,
  },
  listItem: {
    backgroundColor: theme.colors.surface,
    padding: theme.spacing.md,
    borderRadius: theme.border.radius.md,
    marginBottom: theme.spacing.sm,
    ...theme.shadows.sm,
  },
  listTitle: {
    fontSize: 16,
    color: theme.colors.text,
  },
});
