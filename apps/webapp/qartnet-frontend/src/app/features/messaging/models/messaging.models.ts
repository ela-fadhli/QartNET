export type ConversationType = 'DIRECT' | 'GROUP';

export interface ConversationParticipantResponse {
  userPublicId: string;
  username: string;
  profilePictureUrl: string | null;
}

export interface MessageResponse {
  publicId: string;
  conversationPublicId: string;
  senderPublicId: string;
  senderUsername: string;
  body: string;
  sentAt: string;
  editedAt: string | null;
}

export interface ConversationSummaryResponse {
  publicId: string;
  type: ConversationType;
  name: string | null;
  participants: ConversationParticipantResponse[];
  lastMessage: MessageResponse | null;
  unreadCount: number;
  updatedAt: string;
}

export interface SendMessageRequest {
  body: string;
}
