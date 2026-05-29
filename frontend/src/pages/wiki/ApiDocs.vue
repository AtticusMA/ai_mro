<template>
  <div class="api-docs">
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span class="card-title">API 契约文档</span>
          <el-tag size="small" effect="plain">{{ totalEndpoints }} 个端点 · {{ groups.length }} 个模块</el-tag>
        </div>
      </template>

      <el-collapse v-model="activeGroups">
        <el-collapse-item
          v-for="g in groups"
          :key="g.domain"
          :title="g.domain"
          :name="g.domain"
        >
          <template #title>
            <div class="group-title">
              <span class="group-name">{{ g.domain }}</span>
              <el-tag size="small" effect="plain" class="group-count">{{ g.endpoints.length }}</el-tag>
            </div>
          </template>
          <div class="endpoint-list">
            <div v-for="(ep, i) in g.endpoints" :key="i" class="endpoint">
              <span class="method-badge" :class="`m-${ep.method.toLowerCase()}`">{{ ep.method }}</span>
              <span class="ep-url mono">{{ ep.url }}</span>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getApiDocs } from '@/api/wiki'

const groups = ref([])
const activeGroups = ref([])

const totalEndpoints = computed(() => groups.value.reduce((sum, g) => sum + g.endpoints.length, 0))

onMounted(async () => {
  try {
    const res = await getApiDocs()
    groups.value = res.data || []
    activeGroups.value = groups.value.map(g => g.domain)
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.card-head { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: 600; }
.group-title { display: flex; align-items: center; gap: 8px; }
.group-name { font-weight: 600; text-transform: capitalize; }
.group-count { font-size: 10px; }
.endpoint-list { display: flex; flex-direction: column; gap: 6px; }
.endpoint { display: flex; align-items: center; gap: 10px; padding: 6px 0; border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5); }
.endpoint:last-child { border-bottom: 0; }
.mono { font-family: 'JetBrains Mono', Consolas, monospace; font-size: 12.5px; }
.method-badge {
  display: inline-flex; align-items: center; justify-content: center;
  width: 56px; height: 22px;
  font-family: 'JetBrains Mono', Consolas, monospace; font-size: 10px; font-weight: 700;
  letter-spacing: 0.5px; border-radius: 3px; flex-shrink: 0;
}
.m-get    { background: #e6f7e6; color: #16a34a; }
.m-post   { background: #e6f0ff; color: #2563eb; }
.m-put    { background: #fff5e6; color: #d97706; }
.m-delete { background: #fee2e2; color: #dc2626; }
.m-patch  { background: #f3e8ff; color: #7c3aed; }
.ep-url { color: var(--el-text-color-primary, #303133); }
</style>
