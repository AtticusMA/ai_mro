<template>
  <div class="manual-translate">
    <el-row :gutter="16">
      <!-- 左侧：提交翻译任务 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span class="title">提交翻译任务</span></template>
          <el-form :model="form" label-width="80px">
            <el-form-item label="选择手册">
              <el-select v-model="form.manualId" placeholder="请选择已解析手册" style="width:100%"
                @change="handleManualChange">
                <el-option v-for="m in parsedManuals" :key="m.id" :label="`${m.manualNo} — ${m.aircraftType}`"
                  :value="m.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="源语言">
              <el-select v-model="form.sourceLang" style="width:100%">
                <el-option label="英文 (en)" value="en" />
              </el-select>
            </el-form-item>
            <el-form-item label="目标语言">
              <el-select v-model="form.targetLang" style="width:100%">
                <el-option label="中文 (zh)" value="zh" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" :disabled="!form.manualId"
                @click="handleSubmit" v-permission="['manual:translate']">
                提交翻译
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider>翻译进度</el-divider>
          <el-empty v-if="taskList.length === 0" description="暂无翻译任务" :image-size="60" />
          <div v-for="task in taskList" :key="task.taskId" class="task-item">
            <div class="task-header">
              <span class="task-id">任务 #{{ task.taskId }}</span>
              <el-tag size="small" :type="taskStatusType(task.status)">{{ taskStatusLabel(task.status) }}</el-tag>
            </div>
            <el-progress v-if="task.status === 'processing'" :percentage="task.progress || 60" status="striped"
              striped-flow :duration="6" />
            <div v-if="task.status === 'completed'" class="task-result">
              <span class="accuracy">准确率：{{ (task.accuracyScore * 100).toFixed(1) }}%</span>
              <el-button type="primary" link @click="viewTranslation(task)">查看译文</el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：中英对照查看 -->
      <el-col :span="14">
        <el-card shadow="never" class="comparison-card">
          <template #header>
            <span class="title">中英对照</span>
            <el-tag v-if="currentTask" size="small" :type="taskStatusType(currentTask.status)">
              {{ taskStatusLabel(currentTask.status) }}
            </el-tag>
          </template>

          <el-empty v-if="!currentTask" description="请从左侧选择已完成的翻译任务" />

          <div v-else>
            <div v-if="currentTask.status === 'completed'" class="comparison-info">
              <el-alert type="success" :closable="false" show-icon>
                翻译准确率：<strong>{{ (currentTask.accuracyScore * 100).toFixed(1) }}%</strong>
                （航空术语专项评估）
              </el-alert>
            </div>

            <el-table v-if="currentTask.status === 'completed'" :data="comparisonData" border class="comparison-table">
              <el-table-column label="章节" prop="chapter" width="100" align="center" />
              <el-table-column label="英文原文">
                <template #default="{ row }">
                  <div class="lang-text en-text">{{ row.en }}</div>
                </template>
              </el-table-column>
              <el-table-column label="中文译文">
                <template #default="{ row }">
                  <div class="lang-text zh-text">{{ row.zh }}</div>
                </template>
              </el-table-column>
            </el-table>

            <el-result v-else-if="currentTask.status === 'processing'"
              icon="info" title="翻译处理中" sub-title="翻译任务正在执行，请稍候...">
              <template #extra>
                <el-button @click="refreshTask(currentTask)">刷新状态</el-button>
              </template>
            </el-result>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getManualList, submitTranslation, getTranslationResult } from '@/api/manual'

const parsedManuals = ref([])
const form = reactive({ manualId: null, sourceLang: 'en', targetLang: 'zh' })
const submitting = ref(false)
const taskList = ref([])
const currentTask = ref(null)

const comparisonData = [
  {
    chapter: '29-10-01',
    en: 'Disconnect the hydraulic pump electrical connector before removal. Torque value: 45-50 Nm. Refer to AMM 29-10-01 P203 for detailed procedure.',
    zh: '拆卸前断开液压泵电气接头。力矩值：45-50 牛·米。详细程序参见 AMM 29-10-01 P203。'
  },
  {
    chapter: '72-00-00',
    en: 'Ensure the engine is at ambient temperature prior to performing engine removal. Use approved sling assembly P/N 9980003-01.',
    zh: '执行发动机拆卸前确认发动机处于环境温度。使用经批准的吊装组件，件号 9980003-01。'
  },
  {
    chapter: '32-40-00',
    en: 'Retract the landing gear and verify GEAR UNSAFE light extinguishes within 5 seconds of gear retraction initiation.',
    zh: '收起起落架并验证起落架不安全灯在收起动作开始后 5 秒内熄灭。'
  }
]

const taskStatusType = (s) => ({ pending: 'info', processing: 'warning', completed: 'success', failed: 'danger' }[s] || 'info')
const taskStatusLabel = (s) => ({ pending: '等待中', processing: '翻译中', completed: '已完成', failed: '失败' }[s] || s)

let pollTimer = null

const loadManuals = async () => {
  const res = await getManualList({ pageNum: 1, pageSize: 50, parsedStatus: 'parsed' })
  if (res.code === 200) parsedManuals.value = res.data.list
}

const handleManualChange = () => {}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await submitTranslation(form.manualId, { sourceLang: form.sourceLang, targetLang: form.targetLang })
    if (res.code === 200) {
      ElMessage.success('翻译任务已提交，正在处理中...')
      const task = { taskId: res.data.taskId, status: 'processing', progress: 10, accuracyScore: 0 }
      taskList.value.unshift(task)
      startPoll(task)
    }
  } finally {
    submitting.value = false
  }
}

const startPoll = (task) => {
  pollTimer = setInterval(async () => {
    const res = await getTranslationResult(task.taskId)
    if (res.code === 200) {
      Object.assign(task, res.data)
      if (res.data.status === 'completed' || res.data.status === 'failed') {
        clearInterval(pollTimer)
        if (res.data.status === 'completed') ElMessage.success('翻译完成！')
      } else {
        task.progress = Math.min((task.progress || 10) + 15, 90)
      }
    }
  }, 3000)
}

const viewTranslation = (task) => { currentTask.value = task }
const refreshTask = async (task) => {
  const res = await getTranslationResult(task.taskId)
  if (res.code === 200) Object.assign(task, res.data)
}

onMounted(loadManuals)
onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })
</script>

<style scoped>
.title { font-weight: 600; }
.task-item { padding: 10px; border: 1px solid #ebeef5; border-radius: 6px; margin-bottom: 8px; }
.task-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.task-id { font-size: 13px; color: #606266; }
.task-result { display: flex; align-items: center; justify-content: space-between; margin-top: 4px; }
.accuracy { font-size: 13px; color: #67c23a; }
.comparison-card :deep(.el-card__header) { display: flex; align-items: center; gap: 10px; }
.comparison-info { margin-bottom: 12px; }
.comparison-table { margin-top: 8px; }
.lang-text { font-size: 13px; line-height: 1.6; padding: 4px 0; }
.en-text { color: #303133; }
.zh-text { color: #409eff; }
</style>
