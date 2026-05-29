<template>
  <div class="manual-list">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="机型">
          <el-select v-model="queryParams.aircraftType" placeholder="全部机型" clearable style="width:140px">
            <el-option v-for="t in aircraftTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="解析状态">
          <el-select v-model="queryParams.parsedStatus" placeholder="全部状态" clearable style="width:120px">
            <el-option label="已解析" value="parsed" />
            <el-option label="待解析" value="pending" />
            <el-option label="解析失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="title">维修手册管理</span>
          <el-button type="primary" @click="uploadVisible = true" v-permission="['manual:upload']">
            <el-icon><Upload /></el-icon>上传手册
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="manualNo" label="手册编号" width="180" show-overflow-tooltip />
        <el-table-column prop="title" label="手册名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="aircraftType" label="适用机型" width="120" align="center" />
        <el-table-column prop="format" label="格式" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="formatTagType(row.format)">{{ row.format }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parsedStatus" label="解析状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.parsedStatus)">{{ statusLabel(row.parsedStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploadedAt" label="上传时间" width="120">
          <template #default="{ row }">{{ formatDate(row.uploadedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">阅读</el-button>
            <el-button type="primary" link @click="handleSearch2(row)" v-permission="['manual:search']">搜索</el-button>
            <el-button type="warning" link @click="handleParse(row)"
              v-if="row.parsedStatus !== 'parsed'" v-permission="['manual:upload']">解析</el-button>
            <el-button type="success" link @click="handlePublish(row)"
              v-if="row.parsedStatus === 'parsed'" v-permission="['manual:publish']">发布</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-permission="['manual:upload']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        @change="fetchData"
      />
    </el-card>

    <!-- 上传手册弹窗 -->
    <el-dialog v-model="uploadVisible" title="上传维修手册" width="520px" :close-on-click-modal="false">
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="90px">
        <el-form-item label="手册标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="如：B737NG AMM 修订版本 47" />
        </el-form-item>
        <el-form-item label="手册编号" prop="manualNo">
          <el-input v-model="uploadForm.manualNo" placeholder="如：AMM-B737-47（唯一）" />
        </el-form-item>
        <el-form-item label="适用机型" prop="aircraftType">
          <el-select v-model="uploadForm.aircraftType" placeholder="请选择" style="width:100%">
            <el-option v-for="t in aircraftTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件格式" prop="format">
          <el-radio-group v-model="uploadForm.format">
            <el-radio value="PDF">PDF</el-radio>
            <el-radio value="XML">XML</el-radio>
            <el-radio value="SGML">SGML</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手册文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="() => (uploadForm.file = null)"
            accept=".pdf,.xml,.sgm,.sgml"
          >
            <el-button type="primary" plain><el-icon><UploadFilled /></el-icon>选择文件</el-button>
            <template #tip>
              <div class="upload-tip">支持 PDF / XML / SGML，最大 500MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Upload, UploadFilled } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getManualList, uploadManual, deleteManual, triggerParse, publishManual } from '@/api/manual'

const router = useRouter()
const aircraftTypes = ['B737-800', 'A320neo', 'B777-300ER']

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 20, aircraftType: '', parsedStatus: '' })

const uploadVisible = ref(false)
const uploading = ref(false)
const uploadFormRef = ref(null)
const uploadRef = ref(null)
const uploadForm = reactive({ title: '', manualNo: '', aircraftType: '', format: 'PDF', file: null })
const uploadRules = {
  title: [{ required: true, message: '请输入手册标题' }],
  manualNo: [{ required: true, message: '请输入手册编号' }],
  aircraftType: [{ required: true, message: '请选择机型' }],
  format: [{ required: true, message: '请选择格式' }],
  file: [{ required: true, message: '请选择文件', validator: (_, __, cb) => (uploadForm.file ? cb() : cb(new Error('请选择文件'))) }]
}

const formatTagType = (fmt) => ({ PDF: '', XML: 'success', SGML: 'warning' }[fmt] || 'info')
const statusTagType = (s) => ({ parsed: 'success', pending: 'warning', failed: 'danger' }[s] || 'info')
const statusLabel = (s) => ({ parsed: '已解析', pending: '待解析', failed: '解析失败' }[s] || s)
const formatDate = (iso) => (iso ? iso.slice(0, 10) : '-')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getManualList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.pageNum = 1; fetchData() }
const handleReset = () => { queryParams.aircraftType = ''; queryParams.parsedStatus = ''; handleSearch() }

const handleView = (row) => router.push({ name: 'ManualReader', params: { id: row.id } })
const handleSearch2 = (row) => router.push({ name: 'ManualSearch', query: { manualId: row.id } })

const handleParse = async (row) => {
  await triggerParse(row.id)
  ElMessage.success('已触发解析，请稍后刷新查看状态')
}

const handlePublish = async (row) => {
  await ElMessageBox.confirm(`确认发布手册「${row.title}」？`, '发布确认', { type: 'warning' })
  await publishManual(row.id)
  ElMessage.success('发布成功')
  fetchData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除手册「${row.title}」？删除后不可恢复。`, '删除确认', { type: 'warning' })
  await deleteManual(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

const handleFileChange = (file) => { uploadForm.file = file.raw }

const handleUpload = async () => {
  await uploadFormRef.value.validate()
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('title', uploadForm.title)
    fd.append('manualNo', uploadForm.manualNo)
    fd.append('aircraftType', uploadForm.aircraftType)
    fd.append('format', uploadForm.format)
    if (uploadForm.file) fd.append('file', uploadForm.file)
    const res = await uploadManual(fd)
    if (res.code === 200) {
      ElMessage.success('上传成功，系统将自动解析')
      uploadVisible.value = false
      uploadFormRef.value.resetFields()
      uploadRef.value?.clearFiles()
      uploadForm.file = null
      fetchData()
    }
  } finally {
    uploading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
.upload-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
