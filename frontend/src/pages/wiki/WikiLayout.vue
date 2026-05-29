<template>
  <div class="wiki-layout">
    <aside class="wiki-sidebar">
      <div class="sidebar-search">
        <el-input
          v-model="searchQuery"
          placeholder="搜索文档..."
          :prefix-icon="Search"
          clearable
          size="small"
          @input="handleSearch"
        />
      </div>
      <div class="sidebar-filters">
        <el-select v-model="filterDomain" placeholder="领域" clearable size="small" class="filter-select" @change="handleSearch">
          <el-option label="认证" value="auth" />
          <el-option label="系统管理" value="system" />
          <el-option label="平台" value="platform" />
          <el-option label="MRO" value="mro" />
          <el-option label="ADR" value="adr" />
        </el-select>
        <el-select v-model="filterType" placeholder="类型" clearable size="small" class="filter-select" @change="handleSearch">
          <el-option label="Spec" value="spec" />
          <el-option label="Plan" value="plan" />
          <el-option label="Tasks" value="tasks" />
          <el-option label="ADR" value="adr" />
          <el-option label="Charter" value="charter" />
        </el-select>
      </div>
      <div class="sidebar-tree">
        <SpecTree
          :tree="specTree"
          :current-id="currentSpecId"
          @select="navigateToSpec"
        />
      </div>
    </aside>
    <main class="wiki-main">
      <div class="wiki-tabs">
        <el-radio-group v-model="activeTab" size="small" @change="handleTabChange">
          <el-radio-button label="specs">规格文档</el-radio-button>
          <el-radio-button label="code-mapping">代码映射</el-radio-button>
          <el-radio-button label="ai-chat">问答定位</el-radio-button>
          <el-radio-button label="api-docs">API文档</el-radio-button>
        </el-radio-group>
      </div>
      <div class="wiki-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getSpecs } from '@/api/wiki'
import SpecTree from '@/components/wiki/SpecTree.vue'

const router = useRouter()
const route = useRoute()

const specTree = ref({})
const specList = ref([])
const searchQuery = ref('')
const filterDomain = ref('')
const filterType = ref('')

const currentSpecId = computed(() => route.params.id || '')

const activeTab = computed({
  get() {
    const path = route.path
    if (path.includes('/code-mapping')) return 'code-mapping'
    if (path.includes('/ai-chat')) return 'ai-chat'
    if (path.includes('/api-docs')) return 'api-docs'
    return 'specs'
  },
  set() {},
})

const handleTabChange = (tab) => {
  if (tab === 'specs') router.push('/wiki/specs')
  else router.push(`/wiki/${tab}`)
}

const navigateToSpec = (specId) => {
  router.push(`/wiki/specs/${specId}`)
}

const handleSearch = async () => {
  try {
    const params = {}
    if (searchQuery.value) params.q = searchQuery.value
    if (filterDomain.value) params.domain = filterDomain.value
    if (filterType.value) params.type = filterType.value
    const res = await getSpecs(params)
    specTree.value = res.data.tree || {}
    specList.value = res.data.list || []
  } catch {
    // ignore
  }
}

const loadSpecs = async () => {
  try {
    const res = await getSpecs()
    specTree.value = res.data.tree || {}
    specList.value = res.data.list || []
  } catch {
    // ignore
  }
}

onMounted(loadSpecs)

watch(() => route.path, () => {
  if (route.path === '/wiki/specs') {
    searchQuery.value = ''
    filterDomain.value = ''
    filterType.value = ''
    loadSpecs()
  }
})
</script>

<style scoped>
.wiki-layout {
  display: flex;
  gap: 0;
  height: calc(100vh - 120px);
  margin: -20px;
}
.wiki-sidebar {
  width: 280px;
  flex-shrink: 0;
  border-right: 1px solid var(--el-border-color, #dcdfe6);
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color-page, #f2f3f5);
}
.sidebar-search { padding: 12px 12px 8px; }
.sidebar-filters { padding: 0 12px 8px; display: flex; gap: 6px; }
.filter-select { flex: 1; }
.sidebar-tree { flex: 1; overflow-y: auto; padding: 0 4px; }
.wiki-main { flex: 1; display: flex; flex-direction: column; min-width: 0; overflow: hidden; }
.wiki-tabs { padding: 12px 20px 0; flex-shrink: 0; }
.wiki-content { flex: 1; overflow-y: auto; padding: 16px 20px; }
</style>
