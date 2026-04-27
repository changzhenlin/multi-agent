<script setup lang="ts">
import { ChatLineRound, CirclePlus, Delete, Promotion } from '@element-plus/icons-vue';
import { computed, ref } from 'vue';

interface LocalMessage {
  id: number;
  role: 'user' | 'assistant';
  content: string;
}

const draft = ref('');
const messages = ref<LocalMessage[]>([
  {
    id: 1,
    role: 'assistant',
    content: '多智能体对话骨架已就绪。后续会接入会话、路由和流式 Agent 输出。',
  },
]);

const canSend = computed(() => draft.value.trim().length > 0);

function sendMessage() {
  const content = draft.value.trim();
  if (!content) {
    return;
  }

  messages.value.push({ id: Date.now(), role: 'user', content });
  draft.value = '';
}

function clearMessages() {
  messages.value = [];
}
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="session-sidebar" width="280px">
      <div class="sidebar-header">
        <div>
          <h1>Multi-Agent Chat</h1>
          <p>内部助手</p>
        </div>
        <el-button :icon="CirclePlus" circle aria-label="新建会话" />
      </div>

      <el-menu default-active="demo" class="session-menu">
        <el-menu-item index="demo">
          <el-icon><ChatLineRound /></el-icon>
          <span>当前会话</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="chat-header">
        <div>
          <strong>企业问答</strong>
          <span>Simple / RAG / SQL Agent</span>
        </div>
        <el-button :icon="Delete" text aria-label="清空会话" @click="clearMessages" />
      </el-header>

      <el-main class="chat-main">
        <div class="message-list">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-row"
            :class="`message-row--${message.role}`"
          >
            <div class="message-bubble">
              {{ message.content }}
            </div>
          </div>
        </div>
      </el-main>

      <el-footer class="chat-footer">
        <el-input
          v-model="draft"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 5 }"
          placeholder="输入问题，Enter 发送"
          resize="none"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button type="primary" :icon="Promotion" :disabled="!canSend" @click="sendMessage">
          发送
        </el-button>
      </el-footer>
    </el-container>
  </el-container>
</template>
