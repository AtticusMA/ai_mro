<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  total: {
    type: Number,
    default: 0
  },
  page: {
    type: Number,
    default: 1
  },
  limit: {
    type: Number,
    default: 10
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  }
})

const emit = defineEmits(['update:page', 'update:limit', 'change'])

const currentPage = ref(props.page)
const pageSize = ref(props.limit)

// 监听 props 变化
watch(() => props.page, (newVal) => {
  currentPage.value = newVal
})

watch(() => props.limit, (newVal) => {
  pageSize.value = newVal
})

/**
 * 处理页码变化
 */
const handleCurrentChange = (page) => {
  emit('update:page', page)
  emit('change', { page, limit: pageSize.value })
}

/**
 * 处理每页数量变化
 */
const handleSizeChange = (size) => {
  emit('update:limit', size)
  emit('change', { page: currentPage.value, limit: size })
}

/**
 * 获取分页信息
 */
const getPaginationInfo = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value + 1
  const end = Math.min(currentPage.value * pageSize.value, props.total)
  return {
    start,
    end,
    total: props.total,
    page: currentPage.value,
    limit: pageSize.value
  }
})

defineExpose({
  getPaginationInfo
})
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.pagination-container :deep(.el-pagination) {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.pagination-container :deep(.el-pager li) {
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.pagination-container :deep(.el-pager li:hover) {
  color: #667eea;
}

.pagination-container :deep(.el-pager li.active) {
  background: #667eea;
  color: white;
}

.pagination-container :deep(.el-pagination__btn) {
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  border-radius: 4px;
}

.pagination-container :deep(.el-pagination__btn:hover:not([disabled])) {
  color: #667eea;
}

.pagination-container :deep(.el-pagination__btn[disabled]) {
  color: #ccc;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pagination-container {
    padding: 15px 0;
  }

  .pagination-container :deep(.el-pagination) {
    flex-wrap: wrap;
  }
}
</style>
