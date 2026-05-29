<template>
  <div class="manual-versions">
    <el-card shadow="never" class="filter-card">
      <el-form inline>
        <el-form-item label="手册">
          <el-select v-model="selectedManualId" placeholder="请选择手册" style="width:280px"
            @change="handleManualChange">
            <el-option v-for="m in manualOptions" :key="m.id"
              :label="`${m.manualNo} — ${m.aircraftType}`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-permission="['manual:edit']">
          <el-button type="primary" :disabled="!selectedManualId" @click="createVisible = true">
            <el-icon><Plus /></el-icon>新增修订版本
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="content-row">
      <!-- 版本历史列表 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span class="title">修订历史</span></template>
          <el-empty v-if="!selectedManualId" description="请先选择手册" />
          <el-timeline v-else-if="versionList.length > 0">
            <el-timeline-item
              v-for="ver in versionList"
              :key="ver.id"
              :timestamp="ver.effectiveDate"
              placement="top"
              :type="ver.id === compareA?.id || ver.id === compareB?.id ? 'primary' : ''"
            >
              <el-card shadow="hover" class="version-card" :class="{ selected: ver.id === compareA?.id || ver.id === compareB?.id }">
                <div class="ver-header">
                  <strong class="ver-no">{{ ver.versionNo }}</strong>
                  <el-tag size="small" type="info">{{ ver.revisedByName }}</el-tag>
                  <div class="ver-actions">
                    <el-button size="small" @click="selectCompare(ver)">
                      {{ compareA?.id === ver.id ? '已选为版本A' : compareB?.id === ver.id ? '已选为版本B' : '选为对比' }}
                    </el-button>
                  </div>
                </div>
                <p class="ver-summary">{{ ver.changeSummary }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无版本记录" />
          <Pagination v-if="total > 0" :total="total" v-model:page="pageNum"
            v-model:page-size="pageSize" @change="fetchVersions" />
        </el-card>
      </el-col>

      <!-- 差异对比面板 -->
      <el-col :span="10">
        <el-card shadow="never" class="diff-card">
          <template #header>
            <span class="title">差异对比</span>
            <el-button v-if="compareA && compareB" size="small" type="danger" text @click="clearCompare">清除</el-button>
          </template>

          <el-empty v-if="!compareA || !compareB"
            :description="!compareA ? '请从左侧选择版本A' : '请再选择版本B'" :image-size="60" />

          <div v-else>
            <div class="diff-labels">
              <el-tag type="danger" size="small">旧版本：{{ compareA.versionNo }}</el-tag>
              <el-icon class="diff-arrow"><ArrowRight /></el-icon>
              <el-tag type="success" size="small">新版本：{{ compareB.versionNo }}</el-tag>
            </div>

            <div class="diff-section">
              <div class="diff-label">修订摘要变更</div>
              <div class="diff-block removed">
                <span class="diff-mark">-</span> {{ compareA.changeSummary }}
              </div>
              <div class="diff-block added">
                <span class="diff-mark">+</span> {{ compareB.changeSummary }}
              </div>
            </div>

            <div class="diff-section">
              <div class="diff-label">生效日期</div>
              <div class="diff-block removed"><span class="diff-mark">-</span> {{ compareA.effectiveDate }}</div>
              <div class="diff-block added"><span class="diff-mark">+</span> {{ compareB.effectiveDate }}</div>
            </div>

            <div class="diff-section">
              <div class="diff-label">修订人</div>
              <div class="diff-inline">
                {{ compareA.revisedByName }} <el-icon><ArrowRight /></el-icon> {{ compareB.revisedByName }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增版本弹窗 -->
    <el-dialog v-model="createVisible" title="新增客户化修订版本" width="480px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="版本号" prop="versionNo">
          <el-input v-model="createForm.versionNo" placeholder="如：Rev.49" />
        </el-form-item>
        <el-form-item label="修订摘要" prop="changeSummary">
          <el-input v-model="createForm.changeSummary" type="textarea" :rows="3"
            placeholder="描述本次修订的主要内容" />
        </el-form-item>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker v-model="createForm.effectiveDate" type="date" placeholder="选择生效日期"
            format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, ArrowRight } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getManualList, getManualVersions, createManualVersion } from '@/api/manual'

const manualOptions = ref([])
const selectedManualId = ref(null)
const versionList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const compareA = ref(null)
const compareB = ref(null)

const createVisible = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const createForm = reactive({ versionNo: '', changeSummary: '', effectiveDate: '' })
const createRules = {
  versionNo: [{ required: true, message: '请输入版本号' }],
  changeSummary: [{ required: true, message: '请填写修订摘要' }],
  effectiveDate: [{ required: true, message: '请选择生效日期' }]
}

const fetchManuals = async () => {
  const res = await getManualList({ pageNum: 1, pageSize: 50 })
  if (res.code === 200) manualOptions.value = res.data.list
}

const fetchVersions = async () => {
  if (!selectedManualId.value) return
  const res = await getManualVersions(selectedManualId.value, { pageNum: pageNum.value, pageSize: pageSize.value })
  if (res.code === 200) {
    versionList.value = res.data.list
    total.value = res.data.total
  }
}

const handleManualChange = () => {
  pageNum.value = 1
  compareA.value = null
  compareB.value = null
  fetchVersions()
}

const selectCompare = (ver) => {
  if (compareA.value?.id === ver.id) { compareA.value = null; return }
  if (compareB.value?.id === ver.id) { compareB.value = null; return }
  if (!compareA.value) { compareA.value = ver }
  else if (!compareB.value) { compareB.value = ver }
  else { compareA.value = compareB.value; compareB.value = ver }
}

const clearCompare = () => { compareA.value = null; compareB.value = null }

const handleCreate = async () => {
  await createFormRef.value.validate()
  creating.value = true
  try {
    const res = await createManualVersion(selectedManualId.value, createForm)
    if (res.code === 200) {
      ElMessage.success('修订版本创建成功')
      createVisible.value = false
      createFormRef.value.resetFields()
      fetchVersions()
    }
  } finally {
    creating.value = false
  }
}

onMounted(fetchManuals)
</script>

<style scoped>
.title { font-weight: 600; }
.content-row { margin-top: 16px; }
.version-card { transition: box-shadow 0.2s; }
.version-card.selected { border: 1px solid #409eff; }
.ver-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.ver-no { font-size: 15px; color: #303133; }
.ver-actions { margin-left: auto; }
.ver-summary { font-size: 13px; color: #606266; margin: 0; }
.diff-card { position: sticky; top: 16px; }
.diff-labels { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.diff-arrow { color: #909399; }
.diff-section { margin-bottom: 16px; }
.diff-label { font-size: 12px; color: #909399; margin-bottom: 6px; }
.diff-block { font-size: 13px; padding: 6px 10px; border-radius: 4px; margin-bottom: 4px; }
.diff-block.removed { background: #fef0f0; color: #f56c6c; }
.diff-block.added { background: #f0f9eb; color: #67c23a; }
.diff-mark { font-weight: 700; margin-right: 6px; }
.diff-inline { font-size: 13px; color: #606266; display: flex; align-items: center; gap: 6px; }
</style>
