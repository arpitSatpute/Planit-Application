import React from 'react';
import { View, Text, StyleSheet, SafeAreaView, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { theme } from '../../theme';
import { useQuery } from '@tanstack/react-query';
import apiClient from '../../api/client';
import { useAuthStore } from '../../store/authStore';

export const MessagesScreen = () => {
  const user = useAuthStore(state => state.user);

  // Fetch real conversations
  const { data: conversations, isPending, isError, refetch } = useQuery({
    queryKey: ['conversations'],
    queryFn: () => apiClient.get('/chat/conversations').then(res => res.data.data || []),
  });

  const renderChatItem = ({ item }: any) => {
    // Determine chat participant name dynamically 
    const participantName = item.participantName || item.vendorName || item.userName || 'Unknown User';
    const lastMessage = item.lastMessage || 'Connected. Start chatting!';
    const time = item.updatedAt ? new Date(item.updatedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : 'Just now';
    const unread = item.unreadCount || 0;

    return (
      <TouchableOpacity style={styles.chatItem} activeOpacity={0.7}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{participantName.charAt(0)}</Text>
        </View>
        <View style={styles.chatContent}>
          <View style={styles.chatHeader}>
            <Text style={styles.chatName} numberOfLines={1}>{participantName}</Text>
            <Text style={[styles.chatTime, unread > 0 && styles.unreadTime]}>{time}</Text>
          </View>
          <View style={styles.chatMessageRow}>
            <Text style={[styles.chatMessage, unread > 0 && styles.unreadMessage]} numberOfLines={1}>
              {lastMessage}
            </Text>
            {unread > 0 && (
              <View style={styles.unreadBadge}>
                <Text style={styles.unreadCount}>{unread}</Text>
              </View>
            )}
          </View>
        </View>
      </TouchableOpacity>
    );
  };

  const safeData = Array.isArray(conversations) ? conversations : [];

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Messages</Text>
      </View>
      
      {isPending ? (
        <View style={styles.centerContents}>
          <ActivityIndicator size="large" color={theme.colors.primary} />
        </View>
      ) : isError ? (
        <View style={styles.centerContents}>
          <Text style={styles.errorText}>Could not load conversations.</Text>
          <TouchableOpacity onPress={() => refetch()}><Text style={styles.retryText}>Retry</Text></TouchableOpacity>
        </View>
      ) : safeData.length === 0 ? (
        <View style={styles.centerContents}>
           <Text style={styles.emptyText}>No messages yet.</Text>
        </View>
      ) : (
        <FlatList
          data={safeData}
          keyExtractor={(item, index) => item.id || item.conversationId || index.toString()}
          renderItem={renderChatItem}
          contentContainerStyle={styles.listContainer}
          ItemSeparatorComponent={() => <View style={styles.separator} />}
          refreshing={isPending}
          onRefresh={refetch}
        />
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: theme.colors.surface },
  header: { paddingHorizontal: theme.spacing.lg, paddingVertical: theme.spacing.md, borderBottomWidth: 1, borderBottomColor: theme.colors.border },
  headerTitle: { fontSize: 24, fontWeight: '700', color: theme.colors.primary },
  centerContents: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  errorText: { color: theme.colors.error, marginBottom: theme.spacing.md },
  retryText: { color: theme.colors.accent, fontWeight: '600' },
  emptyText: { color: theme.colors.textMuted, fontSize: 16 },
  listContainer: { paddingTop: theme.spacing.sm },
  chatItem: { flexDirection: 'row', padding: theme.spacing.lg, alignItems: 'center' },
  avatar: { width: 52, height: 52, borderRadius: 26, backgroundColor: theme.colors.background, justifyContent: 'center', alignItems: 'center', borderWidth: 1, borderColor: theme.colors.border },
  avatarText: { fontSize: 22, fontWeight: '600', color: theme.colors.textMuted },
  chatContent: { flex: 1, marginLeft: theme.spacing.md },
  chatHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 4 },
  chatName: { fontSize: 16, fontWeight: '600', color: theme.colors.text, flex: 1, marginRight: theme.spacing.sm },
  chatTime: { fontSize: 12, color: theme.colors.textMuted },
  unreadTime: { color: theme.colors.accent, fontWeight: '600' },
  chatMessageRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  chatMessage: { fontSize: 14, color: theme.colors.textMuted, flex: 1, paddingRight: theme.spacing.md },
  unreadMessage: { color: theme.colors.text, fontWeight: '500' },
  unreadBadge: { backgroundColor: theme.colors.accent, paddingHorizontal: 6, paddingVertical: 2, borderRadius: 10, minWidth: 20, alignItems: 'center' },
  unreadCount: { color: '#FFF', fontSize: 11, fontWeight: '700' },
  separator: { height: 1, backgroundColor: theme.colors.border, marginLeft: 84 }, /* 84 offset skips avatar width spacing */
});
