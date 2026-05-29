<template>
  <div class="spec-browser">
    <!-- 首页模式：无 specId -->
    <template v-if="!specId">
      <el-row :gutter="14" class="stats-row">
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">文档总数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num tone-primary">{{ stats.specs }}</div>
            <div class="stat-label">Spec</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num tone-success">{{ stats.approved }}</div>
            <div class="stat-label">已批准</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num tone-warning">{{ stats.adrs }}</div>
            <div class="stat-label">ADR</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="recent-card">
        <template #header>
          <span class="card-title">全部文档</span>
        </template>
        <el-table :data="allDocs" size="small" stripe @row-click="goToSpec">
          <el-table-column prop="id" label="ID" width="120">
            <template #default="{ row }">
              <span class="mono">{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="200" />
          <el-table-column prop="domain" label="领域" width="100">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ row.domain }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="80">
            <template #default="{ row }">
              <el-tag :type="typeTag(row.type)" size="small" effect="plain">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updated" label="更新" width="110">
            <template #default="{ row }">
              <span class="mono">{{ row.updated || row.created || '—' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <!-- 详情模式：有 specId -->
    <template v-else>
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="8" animated />
      </div>
      <template v-else-if="spec">
        <el-button text size="small" class="back-btn" @click="$router.push('/wiki/specs')">
          ← 返回列表
        </el-button>
        <FrontmatterHeader :data="spec" @navigate="goToSpec" />
        <el-card shadow="never" class="doc-card">
          <MarkdownRenderer :content="spec.body" @link-click="handleLinkClick" />
        </el-card>
      </template>
      <el-empty v-else description="文档不存在" />
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSpecs, getSpecDetail } from '@/api/wiki'
import FrontmatterHeader from '@/components/wiki/FrontmatterHeader.vue'
import MarkdownRenderer from '@/components/wiki/MarkdownRenderer.vue'

const route = useRoute()
const router = useRouter()

const allDocs = ref([])
const spec = ref(null)
const loading = ref(false)

const specId = computed(() => route.params.id || '')

const stats = computed(() => {
  const docs = allDocs.value
  return {
    total: docs.length,
    specs: docs.filter(d => d.type === 'spec').length,
    approved: docs.filter(d => d.status === 'approved' || d.status === 'accepted').length,
    adrs: docs.filter(d => d.type === 'adr').length,
  }
})

const statusTag = (s) => {
  const m = { approved: 'success', accepted: 'success', draft: 'info', deprecated: 'danger', review: 'warning' }
  return m[s] || ''
}
const typeTag = (t) => {
  const m = { spec: 'primary', plan: 'warning', tasks: 'info', adr: 'success', charter: '' }
  return m[t] || ''
}

const goToSpec = (row) => {
  const id = typeof row === 'string' ? row : row.id
  router.push(`/wiki/specs/${id}`)
}

const handleLinkClick = (href) => {
  const match = href.match(/(\w+-\d+)/)
  if (match) goToSpec(match[1])
}

const loadList = async () => {
  try {
    const res = await getSpecs()
    allDocs.value = res.data.list || []
  } catch {
    // ignore
  }
}

const loadDetail = async (id) => {
  loading.value = true
  spec.value = null
  try {
    const res = await getSpecDetail(id)
    spec.value = res.data
  } catch {
    spec.value = null
  } finally {
    loading.value = false
  }
}

watch(specId, (id) => {
  if (id) loadDetail(id)
  else loadList()
}, { immediate: true })

onMounted(() => {
  if (!specId.value) loadList()
})
</script>

<style scoped>
.stats-row { margin-bottom: 14px; }
.stat-card { text-align: center; }
.stat-card :deep(.el-card__body) { padding: 16px; }
.stat-num {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 28px; font-weight: 700; line-height: 1;
  color: var(--el-text-color-primary, #303133);
}
.stat-label { font-size: 12px; color: var(--el-text-color-secondary, #909399); margin-top: 6px; }
.tone-primary { color: var(--el-color-primary, #409eff); }
.tone-success { color: var(--el-color-success, #67c23a); }
.tone-warning { color: var(--el-color-warning, #e6a23c); }
.card-title { font-weight: 600; }
.mono { font-family: 'JetBrains Mono', Consolas, monospace; font-size: 12px; }
.recent-card :deep(.el-table__row) { cursor: pointer; }
.back-btn { margin-bottom: 12px; }
.doc-card { margin-top: 12px; }
.doc-card :deep(.el-card__body) { padding: 20px; }
.loading-wrap { padding: 20px; }
</style>
