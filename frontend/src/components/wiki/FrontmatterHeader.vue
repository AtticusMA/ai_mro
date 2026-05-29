<template>
  <el-descriptions :column="4" border size="small" class="fm-header">
    <el-descriptions-item label="ID">
      <span class="mono">{{ data.id }}</span>
    </el-descriptions-item>
    <el-descriptions-item label="类型">
      <el-tag :type="typeTag" size="small" effect="plain">{{ typeLabel }}</el-tag>
    </el-descriptions-item>
    <el-descriptions-item label="领域">
      <el-tag size="small" effect="plain">{{ data.domain }}</el-tag>
    </el-descriptions-item>
    <el-descriptions-item label="状态">
      <el-tag :type="statusTag" size="small">{{ data.status }}</el-tag>
    </el-descriptions-item>
    <el-descriptions-item label="标题" :span="2">
      {{ data.title }}
    </el-descriptions-item>
    <el-descriptions-item v-if="data.version" label="版本">
      <span class="mono">{{ data.version }}</span>
    </el-descriptions-item>
    <el-descriptions-item v-if="data.owner" label="负责人">
      {{ data.owner }}
    </el-descriptions-item>
    <el-descriptions-item v-if="data.created" label="创建">
      <span class="mono">{{ data.created }}</span>
    </el-descriptions-item>
    <el-descriptions-item v-if="data.updated" label="更新">
      <span class="mono">{{ data.updated }}</span>
    </el-descriptions-item>
    <el-descriptions-item v-if="data.dependsOn?.length" label="依赖" :span="2">
      <el-link
        v-for="dep in data.dependsOn"
        :key="dep"
        type="primary"
        :underline="false"
        class="dep-link"
        @click="$emit('navigate', dep)"
      >{{ dep }}</el-link>
    </el-descriptions-item>
    <el-descriptions-item v-if="data.supersedes?.length" label="替代" :span="2">
      <span class="mono">{{ data.supersedes.join(', ') }}</span>
    </el-descriptions-item>
  </el-descriptions>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Object, default: () => ({}) },
})

defineEmits(['navigate'])

const statusTag = computed(() => {
  const m = { approved: 'success', accepted: 'success', draft: 'info', review: 'warning', deprecated: 'danger', proposed: 'warning' }
  return m[props.data.status] || ''
})

const typeLabel = computed(() => {
  const m = { spec: 'Spec', plan: 'Plan', tasks: 'Tasks', adr: 'ADR', charter: 'Charter' }
  return m[props.data.type] || props.data.type
})

const typeTag = computed(() => {
  const m = { spec: 'primary', plan: 'warning', tasks: 'info', adr: 'success', charter: '' }
  return m[props.data.type] || ''
})
</script>

<style scoped>
.fm-header { margin-bottom: 16px; }
.mono { font-family: 'JetBrains Mono', Consolas, monospace; font-size: 12px; }
.dep-link { margin-right: 8px; font-family: 'JetBrains Mono', Consolas, monospace; font-size: 12px; }
</style>
