<template>
  <div class="md-body" v-html="rendered" @click="handleClick" />
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const props = defineProps({ content: { type: String, default: '' } })
const emit = defineEmits(['link-click'])

marked.setOptions({
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true,
})

const rendered = computed(() => {
  if (!props.content) return ''
  return marked.parse(props.content)
})

const handleClick = (e) => {
  const a = e.target.closest('a')
  if (!a) return
  const href = a.getAttribute('href')
  if (href && !href.startsWith('http') && href.endsWith('.md')) {
    e.preventDefault()
    emit('link-click', href)
  }
}
</script>

<style scoped>
.md-body {
  font-size: 14px;
  line-height: 1.7;
  color: var(--el-text-color-primary, #303133);
  word-break: break-word;
}
.md-body :deep(h1) { font-size: 22px; font-weight: 700; margin: 24px 0 12px; padding-bottom: 8px; border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5); }
.md-body :deep(h2) { font-size: 18px; font-weight: 600; margin: 20px 0 10px; padding-bottom: 6px; border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5); }
.md-body :deep(h3) { font-size: 15px; font-weight: 600; margin: 16px 0 8px; }
.md-body :deep(h4) { font-size: 14px; font-weight: 600; margin: 12px 0 6px; }
.md-body :deep(p) { margin: 8px 0; }
.md-body :deep(ul), .md-body :deep(ol) { padding-left: 24px; margin: 8px 0; }
.md-body :deep(li) { margin: 4px 0; }
.md-body :deep(blockquote) {
  margin: 12px 0; padding: 8px 16px;
  border-left: 3px solid var(--el-color-primary, #409eff);
  background: var(--el-fill-color-lighter, #fafafa);
  color: var(--el-text-color-secondary, #606266);
}
.md-body :deep(code) {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, monospace;
  font-size: 12.5px;
  padding: 2px 6px; border-radius: 3px;
  background: var(--el-fill-color-light, #f5f7fa);
  color: var(--el-color-danger, #f56c6c);
}
.md-body :deep(pre) {
  margin: 12px 0; padding: 14px 16px; border-radius: 6px;
  background: #1e1e2e; overflow-x: auto;
}
.md-body :deep(pre code) {
  padding: 0; background: transparent; color: #cdd6f4; font-size: 12.5px; line-height: 1.6;
}
.md-body :deep(table) {
  width: 100%; border-collapse: collapse; margin: 12px 0; font-size: 13px;
}
.md-body :deep(th), .md-body :deep(td) {
  border: 1px solid var(--el-border-color, #dcdfe6);
  padding: 8px 12px; text-align: left;
}
.md-body :deep(th) {
  background: var(--el-fill-color-light, #f5f7fa);
  font-weight: 600;
}
.md-body :deep(a) { color: var(--el-color-primary, #409eff); text-decoration: none; }
.md-body :deep(a:hover) { text-decoration: underline; }
.md-body :deep(hr) { border: none; border-top: 1px solid var(--el-border-color-lighter, #ebeef5); margin: 16px 0; }
.md-body :deep(img) { max-width: 100%; border-radius: 4px; }
.md-body :deep(input[type="checkbox"]) { margin-right: 6px; }
</style>
