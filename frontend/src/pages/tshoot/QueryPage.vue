<template>
  <div class="query-page">
    <el-row :gutter="16">
      <!-- 左侧：查询输入 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span class="title">智能排故查询</span></template>
          <el-form :model="queryForm" label-position="top">
            <el-form-item label="故障代码">
              <el-input v-model="queryForm.fault_code" placeholder="如 ENG-001" />
            </el-form-item>
            <el-form-item label="故障描述">
              <div style="position: relative; width: 100%">
                <el-input
                  v-model="queryForm.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请描述故障现象，如：发动机运行中振动值持续升高..."
                />
                <el-button
                  class="voice-btn"
                  :type="recording ? 'danger' : 'default'"
                  circle
                  size="small"
                  @click="handleVoiceInput"
                  :loading="recording"
                >
                  <el-icon v-if="!recording"><Microphone /></el-icon>
                </el-button>
              </div>
              <div v-if="recording" class="voice-tip">正在录音，请描述故障现象...</div>
            </el-form-item>
            <el-form-item label="机型">
              <el-select v-model="queryForm.aircraft_type" placeholder="请选择">
                <el-option label="B737-800" value="B737-800" />
                <el-option label="A320neo" value="A320neo" />
                <el-option label="B777-300ER" value="B777-300ER" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="querying" @click="handleQuery">
                提交查询
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：结果展示 -->
      <el-col :span="14">
        <el-card shadow="never" v-if="result">
          <template #header><span class="title">排故方案</span></template>
          <div class="result-section">
            <h4>诊断结论</h4>
            <p class="diagnosis">{{ result.diagnosis }}</p>
          </div>
          <div class="result-section">
            <h4>排故步骤</h4>
            <ol class="steps-list">
              <li v-for="(step, idx) in result.steps" :key="idx">{{ step }}</li>
            </ol>
          </div>
          <div class="result-section">
            <h4>参考来源</h4>
            <el-table :data="result.references" size="small" stripe>
              <el-table-column prop="source" label="文档" width="140" />
              <el-table-column prop="chapter" label="章节" width="140" />
              <el-table-column prop="page" label="页码" width="100" />
              <el-table-column prop="relevance" label="相关度" width="80" align="center">
                <template #default="{ row }">
                  {{ (row.relevance * 100).toFixed(0) }}%
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="result-footer">
            <el-tag type="success">置信度: {{ (result.confidence * 100).toFixed(0) }}%</el-tag>
            <el-tag type="info">关联案例: {{ result.similar_cases }} 条</el-tag>
          </div>
        </el-card>
        <el-card shadow="never" v-else>
          <el-empty description="请输入故障信息进行排故查询" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Microphone } from '@element-plus/icons-vue'
import { submitQuery, getQueryResult } from '@/api/tshoot'

const querying = ref(false)
const result = ref(null)
const recording = ref(false)

const handleVoiceInput = () => {
  if (recording.value) return
  recording.value = true
  setTimeout(() => {
    queryForm.description = '发动机启动时发出异常高频噪音，N2转速波动范围超出正常值，伴随轻微振动增大'
    recording.value = false
    ElMessage.success('语音识别完成')
  }, 2000)
}

const queryForm = reactive({
  fault_code: '',
  description: '',
  aircraft_type: ''
})

const handleQuery = async () => {
  if (!queryForm.description && !queryForm.fault_code) {
    ElMessage.warning('请输入故障代码或描述')
    return
  }
  querying.value = true
  result.value = null
  try {
    const submitRes = await submitQuery(queryForm)
    if (submitRes.code === 200) {
      const resultRes = await getQueryResult(submitRes.data.query_id)
      if (resultRes.code === 200 && resultRes.data.status === 'completed') {
        result.value = resultRes.data.report
      }
    }
  } finally {
    querying.value = false
  }
}
</script>

<style scoped>
.title {
  font-weight: 600;
}
.result-section {
  margin-bottom: 20px;
}
.result-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.diagnosis {
  font-size: 14px;
  color: #606266;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}
.steps-list {
  padding-left: 20px;
  font-size: 14px;
  color: #606266;
}
.steps-list li {
  margin-bottom: 6px;
}
.result-footer {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}
.voice-btn {
  position: absolute;
  right: 10px;
  bottom: 10px;
  z-index: 1;
}
.voice-tip {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 4px;
  animation: blink 1s infinite;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
