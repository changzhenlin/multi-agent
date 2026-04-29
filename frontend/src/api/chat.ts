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
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    buffer = emitSseChunks(buffer, onChunk);
  }

  buffer += decoder.decode();
  if (buffer.trim().length > 0) {
    emitSseEvent(buffer, onChunk);
  }
}

async function parseJsonResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }
  return response.json() as Promise<T>;
}

function emitSseChunks(buffer: string, onChunk: (chunk: string) => void): string {
  let eventBoundary = buffer.indexOf('\n\n');
  while (eventBoundary >= 0) {
    const event = buffer.slice(0, eventBoundary);
    emitSseEvent(event, onChunk);
    buffer = buffer.slice(eventBoundary + 2);
    eventBoundary = buffer.indexOf('\n\n');
  }
  return buffer;
}

function emitSseEvent(event: string, onChunk: (chunk: string) => void): void {
  for (const line of event.split(/\r?\n/)) {
    if (line.startsWith('data:')) {
      onChunk(line.slice(5).trimStart());
    }
  }
}
