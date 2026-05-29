<template>
  <div class="ai-chat">
    <el-card shadow="never" class="chat-card">
      <template #header>
        <div class="card-head">
          <span class="card-title">
            <el-icon><ChatDotRound /></el-icon>
            问答定位
          </span>
          <el-button text size="small" @click="clearChat">清空对话</el-button>
        </div>
      </template>

      <div ref="chatArea" class="chat-area">
        <div v-if="!messages.length" class="chat-welcome">
          <div class="welcome-icon">
            <el-icon :size="48" color="var(--el-color-primary)"><ChatDotRound /></el-icon>
          </div>
          <h3>AI 问答定位</h3>
          <p>描述你遇到的问题，AI 将定位到相关 Spec、代码文件和可能的原因。</p>
          <div class="quick-questions">
            <el-button
              v-for="q in quickQuestions"
              :key="q"
              size="small"
              round
              @click="sendMessage(q)"
            >{{ q }}</el-button>
          </div>
        </div>
        <ChatMessage
          v-for="(msg, i) in messages"
          :key="i"
          :role="msg.role"
          :content="msg.content"
          :related-specs="msg.relatedSpecs"
          :code-files="msg.codeFiles"
          @spec-click="goToSpec"
        />
        <div v-if="loading" class="typing">
          <el-icon class="typing-icon"><Loading /></el-icon>
          AI 正在分析...
        </div>
      </div>
    </el-card>

    <div class="chat-input">
      <el-input
        v-model="inputText"
        placeholder="描述问题，如：用户权限校验失败怎么排查？"
        :disabled="loading"
        @keyup.enter="sendMessage(inputText)"
      >
        <template #append>
          <el-button
            type="primary"
            :loading="loading"
            @click="sendMessage(inputText)"
          >发送</el-button>
        </template>
      </el-input>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Loading } from '@element-plus/icons-vue'
import { askAi } from '@/api/wiki'
import ChatMessage from '@/components/wiki/ChatMessage.vue'

const router = useRouter()
const chatArea = ref(null)
const inputText = ref('')
const messages = ref([])
const loading = ref(false)

const quickQuestions = [
  '用户权限校验失败怎么排查？',
  '登录后 token 过期如何处理？',
  '工卡签放流程是怎样的？',
  '如何切换系统主题？',
  '菜单不显示怎么排查？',
]

const scrollToBottom = async () => {
  await nextTick()
  if (chatArea.value) {
    chatArea.value.scrollTop = chatArea.value.scrollHeight
  }
}

const sendMessage = async (text) => {
  const msg = text?.trim()
  if (!msg || loading.value) return

  messages.value.push({ role: 'user', content: msg })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res = await askAi(msg)
    messages.value.push({
      role: 'assistant',
      content: res.data.answer || '暂无回复',
      relatedSpecs: res.data.relatedSpecs || [],
      codeFiles: res.data.codeFiles || [],
    })
  } catch {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，请求失败，请稍后重试。',
      relatedSpecs: [],
      codeFiles: [],
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const clearChat = () => { messages.value = [] }

const goToSpec = (specId) => {
  router.push(`/wiki/specs/${specId}`)
}
</script>

<style scoped>
.ai-chat { display: flex; flex-direction: column; height: 100%; }
.chat-card { flex: 1; display: flex; flex-direction: column; }
.chat-card :deep(.el-card__body) { flex: 1; padding: 0; display: flex; flex-direction: column; overflow: hidden; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.card-title { display: flex; align-items: center; gap: 6px; font-weight: 600; }
.chat-area { flex: 1; overflow-y: auto; padding: 16px 20px; }
.chat-welcome { text-align: center; padding: 40px 20px; color: var(--el-text-color-secondary, #909399); }
.welcome-icon { margin-bottom: 12px; }
.chat-welcome h3 { font-size: 18px; color: var(--el-text-color-primary, #303133); margin-bottom: 8px; }
.chat-welcome p { font-size: 13px; margin-bottom: 16px; }
.quick-questions { display: flex; flex-wrap: wrap; justify-content: center; gap: 8px; }
.typing {
  display: flex; align-items: center; gap: 6px;
  color: var(--el-text-color-secondary, #909399); font-size: 13px; padding: 8px 0;
}
.typing-icon { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.chat-input { padding: 12px 0 0; flex-shrink: 0; }
</style>
