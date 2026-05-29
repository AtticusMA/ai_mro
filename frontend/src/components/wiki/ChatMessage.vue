<template>
  <div class="chat-msg" :class="{ 'is-user': role === 'user' }">
    <div class="msg-avatar">
      <el-avatar :size="32" :style="avatarStyle">
        {{ role === 'user' ? '我' : 'AI' }}
      </el-avatar>
    </div>
    <div class="msg-body">
      <div class="msg-bubble">
        <MarkdownRenderer v-if="role === 'assistant'" :content="content" @link-click="$emit('link-click', $event)" />
        <span v-else>{{ content }}</span>
      </div>
      <div v-if="relatedSpecs?.length" class="msg-refs">
        <span class="ref-label">相关规格：</span>
        <el-link
          v-for="s in relatedSpecs"
          :key="s"
          type="primary"
          :underline="false"
          class="ref-link"
          @click="$emit('spec-click', s)"
        >{{ s }}</el-link>
      </div>
      <div v-if="codeFiles?.length" class="msg-refs">
        <span class="ref-label">相关代码：</span>
        <span v-for="f in codeFiles" :key="f" class="code-file mono">{{ f }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import MarkdownRenderer from '@/components/wiki/MarkdownRenderer.vue'

const props = defineProps({
  role: { type: String, default: 'user' },
  content: { type: String, default: '' },
  relatedSpecs: { type: Array, default: () => [] },
  codeFiles: { type: Array, default: () => [] },
})

defineEmits(['link-click', 'spec-click'])

const avatarStyle = computed(() =>
  props.role === 'user'
    ? { background: 'var(--el-color-primary, #409eff)', color: '#fff' }
    : { background: 'var(--el-color-success, #67c23a)', color: '#fff' },
)
</script>

<style scoped>
.chat-msg { display: flex; gap: 10px; margin-bottom: 16px; }
.chat-msg.is-user { flex-direction: row-reverse; }
.msg-avatar { flex-shrink: 0; }
.msg-body { max-width: 75%; min-width: 0; }
.msg-bubble {
  padding: 10px 14px; border-radius: 8px;
  background: var(--el-fill-color-light, #f5f7fa);
  color: var(--el-text-color-primary, #303133);
  font-size: 13.5px; line-height: 1.6;
}
.is-user .msg-bubble {
  background: var(--el-color-primary-light-8, #d9ecff);
}
.msg-refs {
  margin-top: 6px; padding: 0 4px;
  font-size: 12px; color: var(--el-text-color-secondary, #909399);
  display: flex; flex-wrap: wrap; align-items: center; gap: 4px;
}
.ref-label { flex-shrink: 0; }
.ref-link { font-family: 'JetBrains Mono', Consolas, monospace; font-size: 12px; }
.code-file {
  padding: 1px 6px; border-radius: 3px;
  background: var(--el-fill-color-light, #f5f7fa);
  font-size: 11px;
}
.mono { font-family: 'JetBrains Mono', Consolas, monospace; }
</style>
