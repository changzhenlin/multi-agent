<script setup lang="ts">
import {
  ChatLineRound,
  CirclePlus,
  Delete,
  Loading,
  Promotion,
  Refresh,
} from '@element-plus/icons-vue';
import { computed, nextTick, onMounted, ref } from 'vue';

import {
  createSession,
  deleteSession,
  getSessionMessages,
  getSessions,
  streamChat,
} from './api/chat';
import type { ChatMessageResponse, SessionSummaryResponse } from './types/chat';
import { renderMarkdown } from './utils/markdown';

type MessageRole = 'user' | 'assistant' | 'system';

interface LocalMessage {
  id: string;
  role: MessageRole;
  content: string;
  pending?: boolean;
}

const USER_ID = 'demo-user';

const draft = ref('');
const sessions = ref<SessionSummaryResponse[]>([]);
const currentSessionId = ref('');
const messages = ref<LocalMessage[]>([]);
const loadingSessions = ref(false);
const loadingMessages = ref(false);
const sending = ref(false);
const errorMessage = ref('');
const messageListRef = ref<HTMLElement | null>(null);

const canSend = computed(() => draft.value.trim().length > 0 && !sending.value);
const currentSession = computed(() =>
  sessions.value.find((session) => session.id === currentSessionId.value),
);

onMounted(async () => {
  await refreshSessions();
  if (sessions.value.length > 0) {
    await selectSession(sessions.value[0].id);
  } else {
    await startNewSession();
  }
});

async function refreshSessions() {
  loadingSessions.value = true;
  errorMessage.value = '';
  try {
    sessions.value = await getSessions();
  } catch (error) {
    errorMessage.value = toErrorMessage(error);
  } finally {
    loadingSessions.value = false;
  }
}

async function startNewSession() {
  errorMessage.value = '';
  try {
    const session = await createSession({ userId: USER_ID, title: 'New chat' });
    sessions.value = [session, ...sessions.value.filter((item) => item.id !== session.id)];
    currentSessionId.value = session.id;
    messages.value = [];
  } catch (error) {
    errorMessage.value = toErrorMessage(error);
  }
}

async function selectSession(sessionId: string) {
  currentSessionId.value = sessionId;
  loadingMessages.value = true;
  errorMessage.value = '';
  try {
    const remoteMessages = await getSessionMessages(sessionId);
    messages.value = remoteMessages.map(toLocalMessage);
    await scrollToBottom();
  } catch (error) {
    errorMessage.value = toErrorMessage(error);
  } finally {
    loadingMessages.value = false;
  }
}

async function sendMessage() {
  const content = draft.value.trim();
  if (!content || sending.value) {
    return;
  }

  if (!currentSessionId.value) {
    await startNewSession();
  }
  if (!currentSessionId.value) {
    return;
  }

  const userMessage: LocalMessage = {
    id: crypto.randomUUID(),
    role: 'user',
    content,
  };
  const assistantMessage: LocalMessage = {
    id: crypto.randomUUID(),
    role: 'assistant',
    content: '',
    pending: true,
  };

  messages.value.push(userMessage, assistantMessage);
  draft.value = '';
  sending.value = true;
  errorMessage.value = '';
  await scrollToBottom();

  try {
    await streamChat(
      {
        sessionId: currentSessionId.value,
        userId: USER_ID,
        message: content,
      },
      async (chunk) => {
        assistantMessage.content += chunk;
        await scrollToBottom();
      },
    );
    assistantMessage.pending = false;
    await refreshSessions();
  } catch (error) {
    assistantMessage.pending = false;
    if (!assistantMessage.content) {
      assistantMessage.content = '请求失败，请稍后重试。';
    }
    errorMessage.value = toErrorMessage(error);
  } finally {
    sending.value = false;
  }
}

async function removeCurrentSession() {
  if (!currentSessionId.value) {
    messages.value = [];
    return;
  }

  const removedSessionId = currentSessionId.value;
  errorMessage.value = '';
  try {
    await deleteSession(removedSessionId);
    sessions.value = sessions.value.filter((session) => session.id !== removedSessionId);
    const nextSession = sessions.value[0];
    if (nextSession) {
      await selectSession(nextSession.id);
    } else {
      currentSessionId.value = '';
      messages.value = [];
      await startNewSession();
    }
  } catch (error) {
    errorMessage.value = toErrorMessage(error);
  }
}

function toLocalMessage(message: ChatMessageResponse): LocalMessage {
  return {
    id: message.id,
    role: message.role,
    content: message.content,
  };
}

function formatSessionTime(value: string): string {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

async function scrollToBottom() {
  await nextTick();
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
  }
}

function toErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : '请求失败';
}
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="session-sidebar" width="300px">
      <div class="sidebar-header">
        <div>
          <h1>Multi-Agent Chat</h1>
          <p>企业内部助手</p>
        </div>
        <el-button :icon="CirclePlus" circle aria-label="新建会话" @click="startNewSession" />
      </div>

      <div class="session-toolbar">
        <span>{{ sessions.length }} 个会话</span>
        <el-button :icon="Refresh" text aria-label="刷新会话" @click="refreshSessions" />
      </div>

      <div v-loading="loadingSessions" class="session-list">
        <button
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ 'session-item--active': session.id === currentSessionId }"
          type="button"
          @click="selectSession(session.id)"
        >
          <el-icon><ChatLineRound /></el-icon>
          <span class="session-item__title">{{ session.title }}</span>
          <time>{{ formatSessionTime(session.updatedAt) }}</time>
        </button>
      </div>
    </el-aside>

    <el-container class="chat-column">
      <el-header class="chat-header">
        <div>
          <strong>{{ currentSession?.title ?? '新会话' }}</strong>
          <span>Simple / RAG / SQL Agent</span>
        </div>
        <el-button :icon="Delete" text aria-label="删除当前会话" @click="removeCurrentSession" />
      </el-header>

      <el-alert
        v-if="errorMessage"
        class="error-alert"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="true"
        @close="errorMessage = ''"
      />

      <el-main ref="messageListRef" v-loading="loadingMessages" class="chat-main">
        <div v-if="messages.length === 0" class="empty-state">
          <h2>开始一次对话</h2>
          <p>输入日常问题、制度流程或数据查询，后端会根据意图路由到对应 Agent。</p>
        </div>

        <div v-else class="message-list">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-row"
            :class="`message-row--${message.role}`"
          >
            <div class="message-meta">
              {{ message.role === 'user' ? '你' : '助手' }}
            </div>
            <div class="message-bubble">
              <!-- eslint-disable vue/no-v-html -->
              <div
                v-if="message.role === 'assistant'"
                class="markdown-body"
                v-html="renderMarkdown(message.content || (message.pending ? '思考中...' : ''))"
              />
              <!-- eslint-enable vue/no-v-html -->
              <template v-else>
                {{ message.content }}
              </template>
              <el-icon v-if="message.pending" class="pending-icon">
                <Loading />
              </el-icon>
            </div>
          </div>
        </div>
      </el-main>

      <el-footer class="chat-footer">
        <el-input
          v-model="draft"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 6 }"
          placeholder="输入问题，Enter 发送，Shift+Enter 换行"
          resize="none"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button type="primary" :icon="Promotion" :disabled="!canSend" :loading="sending" @click="sendMessage">
          发送
        </el-button>
      </el-footer>
    </el-container>
  </el-container>
</template>
