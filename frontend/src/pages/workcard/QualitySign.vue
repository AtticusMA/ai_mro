<template>
  <div class="quality-sign">
    <el-card v-loading="loading" shadow="never">
      <template #header>
        <div class="card-header">
          <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
          <span class="title">质检签署 — {{ detail.workcard_no }}</span>
        </div>
      </template>

      <el-descriptions :column="isMobile ? 1 : 3" border class="mb-4">
        <el-descriptions-item label="工卡编号">{{ detail.workcard_no }}</el-descriptions-item>
        <el-descriptions-item label="飞机">{{ detail.aircraft_id }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ { line:'航线', heavy:'定检', troubleshoot:'排故' }[detail.card_type] }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="isMobile ? 1 : 3">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="维修人员">{{ detail.mechanic_name }}</el-descriptions-item>
        <el-descriptions-item label="完工时间">{{ detail.completed_at }}</el-descriptions-item>
        <el-descriptions-item label="签署类型">
          <el-tag :type="detail.sign_type === 'inspector' ? 'warning' : ''" size="small">
            {{ detail.sign_type === 'inspector' ? '检验签署' : '质检签署' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">工序列表</el-divider>
      <el-table :data="detail.steps || []" border size="small" class="mb-4">
        <el-table-column prop="step_no" label="序号" width="60" align="center" />
        <el-table-column prop="description" label="工序描述" min-width="200" />
        <el-table-column prop="mechanic_name" label="执行人" width="100" align="center" />
        <el-table-column prop="completed_at" label="完成时间" width="160" />
        <el-table-column prop="result" label="执行结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.result === 'pass' ? 'success' : 'danger'" size="small">
              {{ row.result === 'pass' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">签署确认</el-divider>
      <el-form :model="signForm" :rules="rules" ref="formRef" label-width="100px" style="max-width:500px">
        <el-form-item label="质检结果" prop="result">
          <el-radio-group v-model="signForm.result">
            <el-radio value="pass">通过</el-radio>
            <el-radio value="fail">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="signForm.remark" type="textarea" :rows="3" placeholder="签署备注（可选）" />
        </el-form-item>
        <el-form-item label="员工号" prop="employee_no">
          <el-input v-model="signForm.employee_no" placeholder="请输入员工号" />
        </el-form-item>
        <el-form-item label="密码确认" prop="password">
          <el-input v-model="signForm.password" type="password" placeholder="请输入密码确认身份" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确认签署</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getSignDetail, submitSign } from '@/api/quality'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const detail = ref({})
const formRef = ref()
const isMobile = computed(() => window.innerWidth < 768)

const signForm = reactive({ result: 'pass', remark: '', employee_no: '', password: '' })

const rules = {
  result: [{ required: true, message: '请选择质检结果' }],
  employee_no: [{ required: true, message: '请输入员工号' }],
  password: [{ required: true, message: '请输入密码' }]
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getSignDetail(route.params.id)
    if (res.code === 200) detail.value = res.data
  } finally { loading.value = false }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const res = await submitSign(route.params.id, signForm)
    if (res.code === 200) {
      ElMessage.success('签署成功')
      router.back()
    } else {
      ElMessage.error(res.message || '签署失败')
    }
  } finally { submitting.value = false }
}

onMounted(fetchDetail)
</script>

<style scoped>
.card-header { display: flex; align-items: center; gap: 12px; }
.card-header .title { font-weight: 600; font-size: 16px; }
.mb-4 { margin-bottom: 16px; }
</style>
