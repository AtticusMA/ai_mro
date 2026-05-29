<template>
  <div class="manual-reader">
    <el-row :gutter="0" class="reader-layout">
      <!-- 左侧：章节目录 -->
      <el-col :span="5" class="sidebar">
        <div class="sidebar-header">
          <div class="manual-info" v-if="manual">
            <div class="manual-title">{{ manual.title }}</div>
            <el-tag size="small" :type="parsedStatusType(manual.parsedStatus)">
              {{ manual.manualNo }}
            </el-tag>
          </div>
          <el-skeleton v-else :rows="2" animated />
        </div>
        <el-scrollbar class="toc-scrollbar">
          <el-tree
            ref="treeRef"
            :data="chapterTree"
            :props="{ label: 'label' }"
            node-key="id"
            highlight-current
            @node-click="handleChapterClick"
            class="toc-tree"
          />
        </el-scrollbar>
      </el-col>

      <!-- 右侧：内容区 -->
      <el-col :span="19" class="reader-content">
        <div class="reader-toolbar">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ name: 'ManualList' }">手册管理</el-breadcrumb-item>
            <el-breadcrumb-item v-if="manual">{{ manual.manualNo }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="activeChapter">{{ activeChapter.number }}</el-breadcrumb-item>
          </el-breadcrumb>

          <div class="toolbar-actions">
            <el-tooltip content="全文搜索">
              <el-button circle @click="goSearch"><el-icon><Search /></el-icon></el-button>
            </el-tooltip>
            <el-tooltip content="翻译管理">
              <el-button circle @click="goTranslate"><el-icon><Translate /></el-icon></el-button>
            </el-tooltip>
            <el-tooltip content="版本历史">
              <el-button circle @click="goVersions"><el-icon><Clock /></el-icon></el-button>
            </el-tooltip>
          </div>
        </div>

        <el-skeleton v-if="loading" :rows="12" animated style="padding:24px" />

        <div v-else-if="activeChapter" class="chapter-content">
          <h2 class="chapter-title">{{ activeChapter.number }} &nbsp; {{ activeChapter.title }}</h2>
          <el-divider />

          <!-- 章节内容（Mock 渲染示意）-->
          <div class="content-body">
            <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px">
              当前章节：<strong>{{ activeChapter.number }} {{ activeChapter.title }}</strong>，
              共 <strong>{{ activeChapter.pageCount }}</strong> 页
            </el-alert>

            <div class="content-section" v-for="(section, i) in mockSections" :key="i">
              <h4>{{ section.heading }}</h4>
              <p>{{ section.body }}</p>
              <el-table v-if="section.table" :data="section.table" border size="small" style="margin:8px 0 16px">
                <el-table-column v-for="col in section.tableCols" :key="col.prop"
                  :prop="col.prop" :label="col.label" :width="col.width" />
              </el-table>
            </div>
          </div>

          <!-- 章节导航 -->
          <div class="chapter-nav">
            <el-button :disabled="!prevChapter" @click="navigateTo(prevChapter)">
              <el-icon><ArrowLeft /></el-icon>上一章节
            </el-button>
            <el-button :disabled="!nextChapter" @click="navigateTo(nextChapter)">
              下一章节<el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>

        <el-empty v-else description="请从左侧目录选择章节" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Clock, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getManualDetail } from '@/api/manual'

// Element Plus 没有内置 Translate 图标，用 Document 代替
const Translate = { render: () => null }

const route = useRoute()
const router = useRouter()
const treeRef = ref(null)

const manual = ref(null)
const chapterTree = ref([])
const activeChapter = ref(null)
const loading = ref(false)

const manualId = computed(() => Number(route.params.id))

const parsedStatusType = (s) => ({ parsed: 'success', pending: 'warning', failed: 'danger' }[s] || 'info')

const prevChapter = computed(() => {
  const idx = chapterTree.value.findIndex((c) => c.id === activeChapter.value?.id)
  return idx > 0 ? chapterTree.value[idx - 1] : null
})
const nextChapter = computed(() => {
  const idx = chapterTree.value.findIndex((c) => c.id === activeChapter.value?.id)
  return idx >= 0 && idx < chapterTree.value.length - 1 ? chapterTree.value[idx + 1] : null
})

const mockSections = computed(() => {
  if (!activeChapter.value) return []
  return [
    {
      heading: '1. 一般说明',
      body: `本章节介绍 ${activeChapter.value.title} 的维修程序和注意事项。操作人员必须持有有效的维修执照，并严格遵循本手册规定的程序执行。`
    },
    {
      heading: '2. 安全警告',
      body: '警告：在执行任何维修工作前，确保相关系统已断电并完成 LOTO（锁定/挂牌）程序。',
    },
    {
      heading: '3. 所需工具和设备',
      body: '执行本章节工作所需工具如下表所示：',
      table: [
        { pn: '9980003-01', name: '吊装组件', qty: '1 套' },
        { pn: '6850000-23', name: '力矩扳手', qty: '1 件' },
        { pn: '2440200-01', name: '接地线', qty: '2 根' }
      ],
      tableCols: [
        { prop: 'pn', label: '件号', width: 140 },
        { prop: 'name', label: '名称', width: 160 },
        { prop: 'qty', label: '数量', width: 80 }
      ]
    },
    {
      heading: '4. 维修程序',
      body: `(1) 确认飞机已固定，所有相关系统已关闭。\n(2) 按照 AMM ${activeChapter.value.number} 规定的力矩值紧固所有螺栓。\n(3) 完工后进行功能测试，确认系统恢复正常工作状态。`
    }
  ]
})

const fetchManual = async () => {
  loading.value = true
  try {
    const res = await getManualDetail(manualId.value)
    if (res.code === 200) {
      manual.value = res.data
      chapterTree.value = (res.data.chapters || []).map((c) => ({
        id: c.id,
        label: `${c.number}  ${c.title}`,
        number: c.number,
        title: c.title,
        pageCount: c.pageCount
      }))

      const targetChapter = route.query.chapter
      if (targetChapter) {
        const found = chapterTree.value.find((c) => c.number === targetChapter)
        if (found) { activeChapter.value = found; return }
      }
      if (chapterTree.value.length > 0) activeChapter.value = chapterTree.value[0]
    }
  } finally {
    loading.value = false
  }
}

const handleChapterClick = (node) => {
  activeChapter.value = node
  router.replace({ query: { ...route.query, chapter: node.number } })
}

const navigateTo = (chapter) => {
  if (chapter) {
    activeChapter.value = chapter
    router.replace({ query: { ...route.query, chapter: chapter.number } })
  }
}

const goSearch = () => router.push({ name: 'ManualSearch', query: { manualId: manualId.value } })
const goTranslate = () => router.push({ name: 'ManualTranslate' })
const goVersions = () => router.push({ name: 'ManualVersions' })

watch(() => route.params.id, fetchManual)
onMounted(fetchManual)
</script>

<style scoped>
.reader-layout { height: calc(100vh - 120px); overflow: hidden; }
.sidebar { border-right: 1px solid #e4e7ed; display: flex; flex-direction: column; height: 100%; }
.sidebar-header { padding: 12px 16px; border-bottom: 1px solid #e4e7ed; }
.manual-title { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 6px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.toc-scrollbar { flex: 1; }
.toc-tree { padding: 8px 0; }
.toc-tree :deep(.el-tree-node__label) { font-size: 12px; }
.reader-content { display: flex; flex-direction: column; height: 100%; overflow: hidden; }
.reader-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 20px; border-bottom: 1px solid #e4e7ed; background: #fafafa;
}
.toolbar-actions { display: flex; gap: 6px; }
.chapter-content { flex: 1; overflow-y: auto; padding: 24px 32px; }
.chapter-title { font-size: 20px; color: #303133; margin: 0 0 4px; }
.content-body { line-height: 1.8; }
.content-section { margin-bottom: 20px; }
.content-section h4 { font-size: 14px; color: #303133; margin: 0 0 8px; }
.content-section p { font-size: 13px; color: #606266; white-space: pre-line; margin: 0; }
.chapter-nav { display: flex; justify-content: space-between; margin-top: 32px; padding-top: 16px;
  border-top: 1px solid #f0f0f0; }
</style>
