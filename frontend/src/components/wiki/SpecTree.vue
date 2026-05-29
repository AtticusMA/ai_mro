<template>
  <el-tree
    :data="treeData"
    :props="{ label: 'label', children: 'children' }"
    :current-node-key="currentId"
    node-key="id"
    highlight-current
    default-expand-all
    class="spec-tree"
    @node-click="handleNodeClick"
  >
    <template #default="{ data: node }">
      <div class="tree-node">
        <span class="node-label">{{ node.label }}</span>
        <el-tag
          v-if="node.status"
          :type="statusType(node.status)"
          size="small"
          class="node-tag"
        >{{ node.status }}</el-tag>
      </div>
    </template>
  </el-tree>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  tree: { type: Object, default: () => ({}) },
  currentId: { type: String, default: '' },
})
const emit = defineEmits(['select'])

const DOMAIN_LABELS = {
  root: '项目',
  auth: '认证 (Auth)',
  system: '系统管理 (System)',
  platform: '平台 (Platform)',
  mro: '智慧机务 (MRO)',
  adr: '架构决策 (ADR)',
}
const TYPE_LABELS = { spec: 'Spec', plan: 'Plan', tasks: 'Tasks', adr: 'ADR', charter: 'Charter', other: '其他' }

const treeData = computed(() => {
  const result = []
  for (const [domain, types] of Object.entries(props.tree)) {
    const domainNode = {
      id: `d_${domain}`,
      label: DOMAIN_LABELS[domain] || domain,
      children: [],
    }
    for (const [type, docs] of Object.entries(types)) {
      const typeNode = {
        id: `t_${domain}_${type}`,
        label: TYPE_LABELS[type] || type,
        children: docs.map(d => ({
          id: d.id,
          label: `${d.id} ${d.title}`,
          status: d.status,
          isDoc: true,
        })),
      }
      domainNode.children.push(typeNode)
    }
    result.push(domainNode)
  }
  return result
})

const statusType = (s) => {
  const m = { approved: 'success', accepted: 'success', draft: 'info', deprecated: 'danger' }
  return m[s] || ''
}

const handleNodeClick = (node) => {
  if (node.isDoc) {
    emit('select', node.id)
  }
}
</script>

<style scoped>
.spec-tree { font-size: 13px; }
.tree-node { display: flex; align-items: center; gap: 6px; width: 100%; }
.node-label { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.node-tag { flex-shrink: 0; font-size: 10px; }
</style>
