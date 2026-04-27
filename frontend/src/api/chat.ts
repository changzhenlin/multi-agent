import type {
  ChatMessageRequest,
  ChatMessageResponse,
  CreateSessionRequest,
  SessionSummaryResponse,
} from '../types/chat';

const jsonHeaders = {
  'Content-Type': 'application/json',
};

export async function getSessions(): Promise<SessionSummaryResponse[]> {
  const response = await fetch('/api/sessions');
  return parseJsonResponse(response);
}

export async function createSession(request: CreateSessionRequest): Promise<SessionSummaryResponse> {
  const response = await fetch('/api/sessions', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(request),
  });
  return parseJsonResponse(response);
}

export async function deleteSession(id: string): Promise<void> {
  const response = await fetch(`/api/sessions/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }
}

export async function getSessionMessages(sessionId: string): Promise<ChatMessageResponse[]> {
  const response = await fetch(`/api/sessions/${encodeURIComponent(sessionId)}/messages`);
  return parseJsonResponse(response);
}

export async function streamChat(
  request: ChatMessageRequest,
  onChunk: (chunk: string) => void,
): Promise<void> {
  const response = await fetch('/api/chat/sse', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(request),
  });

  if (!response.ok || !response.body) {
    throw new Error(`Request failed with status ${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    onChunk(decoder.decode(value, { stream: true }));
  }
}

async function parseJsonResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }
  return response.json() as Promise<T>;
}
