<template>
  <div class="manual-search">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline @submit.prevent="handleSearch">
        <el-form-item>
          <el-input
            v-model="queryParams.q"
            placeholder="输入关键词搜索手册内容（如：液压泵拆装）"
            style="width:400px"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="机型">
          <el-select v-model="queryParams.aircraftType" placeholder="全部" clearable style="width:130px">
            <el-option v-for="t in aircraftTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="content-row">
      <!-- 左侧：搜索结果列表 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span class="title">搜索结果</span>
            <span v-if="total > 0" class="count">共 {{ total }} 条匹配</span>
          </template>

          <el-empty v-if="!loading && resultList.length === 0" description="暂无结果，请输入关键词搜索" />

          <div v-for="item in resultList" :key="`${item.documentId}-${item.chapterRef}`" class="result-item"
            @click="handleOpenChapter(item)">
            <div class="result-header">
              <el-tag size="small" type="primary">{{ item.manualNo }}</el-tag>
              <span class="chapter-ref">{{ item.chapterRef }}</span>
              <el-tag size="small" type="success" class="score-tag">
                相关度 {{ (item.score * 100).toFixed(0) }}%
              </el-tag>
            </div>
            <!-- 高亮片段使用 v-html 渲染 <em> 标签 -->
            <div class="highlight" v-html="item.highlight" />
          </div>

          <Pagination
            v-if="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            @change="fetchResults"
          />
        </el-card>
      </el-col>

      <!-- 右侧：章节导航 -->
      <el-col :span="8">
        <el-card shadow="never" class="toc-card">
          <template #header>
            <span class="title">章节导航</span>
            <el-select v-model="selectedManualId" placeholder="选择手册" size="small"
              @change="loadChapters" style="width:160px">
              <el-option v-for="m in manualOptions" :key="m.id" :label="m.manualNo" :value="m.id" />
            </el-select>
          </template>

          <el-skeleton v-if="chaptersLoading" :rows="8" animated />
          <el-tree
            v-else
            :data="chapterTree"
            :props="{ label: 'label', children: 'children' }"
            @node-click="handleChapterClick"
            highlight-current
            class="chapter-tree"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { searchManuals, getManualList, getManualDetail } from '@/api/manual'

const router = useRouter()
const route = useRoute()
const aircraftTypes = ['B737-800', 'A320neo', 'B777-300ER']

const loading = ref(false)
const resultList = ref([])
const total = ref(0)
const queryParams = reactive({ q: '', aircraftType: '', pageNum: 1, pageSize: 20 })

const manualOptions = ref([])
const selectedManualId = ref(null)
const chapterTree = ref([])
const chaptersLoading = ref(false)

const fetchResults = async () => {
  if (!queryParams.q) return
  loading.value = true
  try {
    const res = await searchManuals(queryParams)
    if (res.code === 200) {
      resultList.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.pageNum = 1; fetchResults() }
const handleReset = () => { queryParams.q = ''; queryParams.aircraftType = ''; resultList.value = []; total.value = 0 }

const handleOpenChapter = (item) => {
  router.push({ name: 'ManualReader', params: { id: item.documentId }, query: { chapter: item.chapterRef } })
}

const loadManualOptions = async () => {
  const res = await getManualList({ pageNum: 1, pageSize: 50, parsedStatus: 'parsed' })
  if (res.code === 200) {
    manualOptions.value = res.data.list
    if (!selectedManualId.value && res.data.list.length > 0) {
      selectedManualId.value = res.data.list[0].id
      loadChapters()
    }
  }
}

const loadChapters = async () => {
  if (!selectedManualId.value) return
  chaptersLoading.value = true
  try {
    const res = await getManualDetail(selectedManualId.value)
    if (res.code === 200) {
      chapterTree.value = (res.data.chapters || []).map((c) => ({
        id: c.id,
        label: `${c.number} ${c.title}`,
        chapterRef: c.number,
        documentId: selectedManualId.value
      }))
    }
  } finally {
    chaptersLoading.value = false
  }
}

const handleChapterClick = (node) => {
  router.push({
    name: 'ManualReader',
    params: { id: node.documentId },
    query: { chapter: node.chapterRef }
  })
}

onMounted(() => {
  if (route.query.q) queryParams.q = route.query.q
  loadManualOptions()
  if (queryParams.q) fetchResults()
})
</script>

<style scoped>
.content-row { margin-top: 16px; }
.title { font-weight: 600; }
.count { font-size: 12px; color: #909399; margin-left: 8px; }
.result-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
}
.result-item:last-child { border-bottom: none; }
.result-item:hover { background: #f5f7fa; }
.result-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.chapter-ref { font-size: 13px; color: #606266; font-family: monospace; }
.score-tag { margin-left: auto; }
.highlight { font-size: 13px; color: #303133; line-height: 1.6; }
:deep(.highlight em) { color: #e6a23c; font-style: normal; font-weight: 600; }
.toc-card :deep(.el-card__header) { display: flex; justify-content: space-between; align-items: center; }
.chapter-tree :deep(.el-tree-node__label) { font-size: 13px; }
</style>
