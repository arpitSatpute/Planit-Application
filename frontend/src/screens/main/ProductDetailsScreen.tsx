import React from 'react';
import { View, Text, StyleSheet, ScrollView, Image, TouchableOpacity, SafeAreaView, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { theme } from '../../theme';
import { Button } from '../../components/Button';

export const ProductDetailsScreen = ({ route, navigation }: any) => {
  const { title = "Grand Plaza Hall", price = "$200 / hr" } = route.params || {};

  return (
    <View style={styles.container}>
      <ScrollView showsVerticalScrollIndicator={false} bounces={false}>
        <View style={styles.imageContainer}>
          <View style={styles.placeholderImg}>
            <Ionicons name="image" size={60} color={theme.colors.border} />
          </View>
          <TouchableOpacity 
            style={styles.backBtn}
            onPress={() => navigation.goBack()}
          >
            <Ionicons name="arrow-back" size={24} color={theme.colors.text} />
          </TouchableOpacity>
          <TouchableOpacity style={styles.likeBtn}>
            <Ionicons name="heart-outline" size={24} color={theme.colors.text} />
          </TouchableOpacity>
        </View>

        <View style={styles.content}>
          <View style={styles.headerRow}>
            <Text style={styles.title}>{title}</Text>
          </View>
          
          <View style={styles.locationRow}>
            <Ionicons name="location" size={16} color={theme.colors.accent} />
            <Text style={styles.locationText}>123 Broadway, New York, NY</Text>
          </View>

          <View style={styles.divider} />

          <View style={styles.hostRow}>
            <View style={styles.hostAvatar}>
              <Text style={styles.hostInitials}>ES</Text>
            </View>
            <View style={styles.hostInfo}>
              <Text style={styles.hostName}>Hosted by Elite Spaces</Text>
              <Text style={styles.hostStats}>Pro Vendor • 120+ Bookings</Text>
            </View>
          </View>

          <View style={styles.divider} />

          <Text style={styles.sectionTitle}>About this venue</Text>
          <Text style={styles.description}>
            The Grand Plaza Hall is an elegant, multi-purpose venue perfect for weddings, corporate events, and large gatherings. It features towering ceilings, crystal chandeliers, and a state-of-the-art sound system.
          </Text>

          <Text style={styles.sectionTitle}>Amenities</Text>
          <View style={styles.amenitiesGrid}>
            {['Wifi', 'Kitchen', 'Parking', 'A/C', 'Projector', 'Security'].map(item => (
              <View key={item} style={styles.amenityItem}>
                <Ionicons name="checkmark-circle" size={20} color={theme.colors.success} />
                <Text style={styles.amenityText}>{item}</Text>
              </View>
            ))}
          </View>
          
          {/* Bottom spacing for sticky footer */}
          <View style={{ height: 100 }} />
        </View>
      </ScrollView>

      <View style={styles.stickyFooter}>
        <View style={styles.priceContainer}>
          <Text style={styles.price}>{price}</Text>
          <Text style={styles.priceSubtitle}>per hour</Text>
        </View>
        <Button 
          title="Book Now"
          style={styles.bookBtn}
          onPress={() => alert('Booking flow initiated')}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.surface,
  },
  imageContainer: {
    width: '100%',
    height: 300,
    backgroundColor: theme.colors.background,
    position: 'relative',
  },
  placeholderImg: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  backBtn: {
    position: 'absolute',
    top: Platform.OS === 'ios' ? 50 : 20,
    left: 20,
    width: 40,
    height: 40,
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    ...theme.shadows.sm,
  },
  likeBtn: {
    position: 'absolute',
    top: Platform.OS === 'ios' ? 50 : 20,
    right: 20,
    width: 40,
    height: 40,
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    ...theme.shadows.sm,
  },
  content: {
    padding: theme.spacing.lg,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: theme.spacing.sm,
  },
  title: {
    fontSize: 26,
    fontWeight: '700',
    color: theme.colors.text,
    flex: 1,
  },
  locationRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: theme.spacing.md,
  },
  locationText: {
    fontSize: 15,
    color: theme.colors.textMuted,
    marginLeft: 4,
  },
  divider: {
    height: 1,
    backgroundColor: theme.colors.border,
    marginVertical: theme.spacing.lg,
  },
  hostRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  hostAvatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: theme.colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
  },
  hostInitials: {
    color: '#FFF',
    fontSize: 18,
    fontWeight: '600',
  },
  hostInfo: {
    marginLeft: theme.spacing.md,
  },
  hostName: {
    fontSize: 16,
    fontWeight: '600',
    color: theme.colors.text,
  },
  hostStats: {
    fontSize: 14,
    color: theme.colors.textMuted,
    marginTop: 2,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: theme.colors.text,
    marginBottom: theme.spacing.sm,
  },
  description: {
    fontSize: 15,
    color: theme.colors.textMuted,
    lineHeight: 22,
    marginBottom: theme.spacing.lg,
  },
  amenitiesGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  amenityItem: {
    flexDirection: 'row',
    alignItems: 'center',
    width: '50%',
    marginBottom: theme.spacing.md,
  },
  amenityText: {
    marginLeft: 8,
    fontSize: 15,
    color: theme.colors.text,
  },
  stickyFooter: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: theme.colors.surface,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: theme.spacing.lg,
    paddingTop: theme.spacing.md,
    paddingBottom: Platform.OS === 'ios' ? 34 : theme.spacing.md,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    ...theme.shadows.md,
  },
  priceContainer: {
    flex: 1,
  },
  price: {
    fontSize: 22,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  priceSubtitle: {
    fontSize: 13,
    color: theme.colors.textMuted,
  },
  bookBtn: {
    paddingHorizontal: theme.spacing.xl,
  },
});
