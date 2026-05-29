<template>
  <div class="code-mapping">
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span class="card-title">Spec ↔ 代码映射</span>
          <el-select v-model="selectedSpec" placeholder="选择 Spec..." filterable clearable size="small" class="spec-select">
            <el-option
              v-for="m in mappings"
              :key="m.specId"
              :label="`${m.specId} — ${m.title}`"
              :value="m.specId"
            />
          </el-select>
        </div>
      </template>

      <el-table :data="currentFiles" size="small" stripe empty-text="选择一个 Spec 查看映射">
        <el-table-column prop="path" label="文件路径" min-width="300">
          <template #default="{ row }">
            <span class="mono">{{ row.path }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="fileTypeTag(row.type)" size="small" effect="plain">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="summary-card">
      <template #header>
        <span class="card-title">映射概览</span>
      </template>
      <el-table :data="summaryData" size="small" stripe>
        <el-table-column prop="specId" label="Spec ID" width="120">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="selectedSpec = row.specId">
              <span class="mono">{{ row.specId }}</span>
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="文件数" width="80">
          <template #default="{ row }">
            <span class="mono">{{ row.files.length }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型分布" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="t in fileTypeSummary(row.files)" :key="t.type" :type="fileTypeTag(t.type)" size="small" effect="plain" class="type-count">
              {{ t.type }}:{{ t.count }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCodeMapping } from '@/api/wiki'

const mappings = ref([])
const selectedSpec = ref('')

const currentFiles = computed(() => {
  if (!selectedSpec.value) return []
  const m = mappings.value.find(m => m.specId === selectedSpec.value)
  return m?.files || []
})

const summaryData = computed(() => mappings.value.filter(m => m.files.length > 0))

const fileTypeTag = (t) => {
  const m = { page: 'primary', api: 'success', mock: 'info', store: 'warning', util: '', component: 'primary', guard: 'danger', config: '' }
  return m[t] || ''
}

const fileTypeSummary = (files) => {
  const counts = {}
  for (const f of files) {
    counts[f.type] = (counts[f.type] || 0) + 1
  }
  return Object.entries(counts).map(([type, count]) => ({ type, count }))
}

onMounted(async () => {
  try {
    const res = await getCodeMapping()
    mappings.value = res.data || []
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.card-head { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: 600; }
.spec-select { width: 320px; }
.mono { font-family: 'JetBrains Mono', Consolas, monospace; font-size: 12px; }
.summary-card { margin-top: 14px; }
.type-count { margin-right: 4px; }
</style>
