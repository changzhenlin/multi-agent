export type AgentType = 'SIMPLE_CHAT' | 'RAG' | 'SERVICE_SQL';

export interface ChatMessageRequest {
  sessionId: string;
  userId: string;
  message: string;
}

export interface CreateSessionRequest {
  userId: string;
  title?: string;
}

export interface SessionSummaryResponse {
  id: string;
  userId: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessageResponse {
  id: string;
  sessionId: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  agentType?: AgentType;
  createdAt: string;
}
