export type ChatRole = 'USER' | 'MODEL';

export interface ChatMessageResponse {
  publicId: string;
  role: ChatRole;
  content: string;
  createdAt: string;
}

export interface AskChatbotRequest {
  message: string;
}

export interface AskChatbotResponse {
  userMessage: ChatMessageResponse;
  assistantMessage: ChatMessageResponse;
}
