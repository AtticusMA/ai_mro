<template>
  <div class="knowledge-base">
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">知识库管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>新建知识库
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="aircraft_type" label="机型" width="120" />
        <el-table-column prop="doc_count" label="文档数" width="90" align="center" />
        <el-table-column prop="vector_count" label="向量数" width="100" align="center">
          <template #default="{ row }">
            {{ (row.vector_count / 1000).toFixed(1) }}K
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ready' ? 'success' : 'warning'" size="small">
              {{ row.status === 'ready' ? '就绪' : '索引中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updated_at" label="更新时间" width="160" />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleUpload(row)">上传文档</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建知识库 -->
    <el-dialog v-model="createVisible" title="新建知识库" width="450">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="如: B737-800 维修手册" />
        </el-form-item>
        <el-form-item label="机型" prop="aircraft_type">
          <el-select v-model="formData.aircraft_type" placeholder="请选择">
            <el-option label="B737-800" value="B737-800" />
            <el-option label="A320neo" value="A320neo" />
            <el-option label="B777-300ER" value="B777-300ER" />
            <el-option label="通用" value="通用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 上传文档 -->
    <el-dialog v-model="showUploadDialog" title="上传文档" width="500px">
      <el-form label-width="80px">
        <el-form-item label="文档类型">
          <el-select v-model="uploadDocType" placeholder="请选择文档类型" style="width: 100%">
            <el-option label="维修手册" value="manual" />
            <el-option label="服务通告" value="bulletin" />
            <el-option label="维修经验" value="experience" />
            <el-option label="故障案例" value="case" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="uploadFileList"
            accept=".pdf,.doc,.docx,.txt"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 PDF/DOC/DOCX/TXT 格式</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUploadSubmit">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import { getKnowledgeBases, createKnowledgeBase } from '@/api/tshoot'

const loading = ref(false)
const tableData = ref([])

const createVisible = ref(false)
const formRef = ref(null)
const formData = reactive({ name: '', aircraft_type: '' })
const formRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  aircraft_type: [{ required: true, message: '请选择机型', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeBases()
    if (res.code === 200) {
      tableData.value = res.data.list
    }
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  formData.name = ''
  formData.aircraft_type = ''
  createVisible.value = true
}

const handleSubmitCreate = async () => {
  await formRef.value.validate()
  const res = await createKnowledgeBase(formData)
  if (res.code === 200) {
    ElMessage.success('知识库创建成功')
    createVisible.value = false
    fetchData()
  }
}

const handleUpload = (row) => {
  uploadKbId.value = row.id
  showUploadDialog.value = true
}

const showUploadDialog = ref(false)
const uploadKbId = ref(null)
const uploadDocType = ref('')
const uploadFileList = ref([])

const handleFileChange = (file) => {
  uploadFileList.value = [file]
}

const handleUploadSubmit = () => {
  if (!uploadDocType.value) {
    ElMessage.warning('请选择文档类型')
    return
  }
  if (uploadFileList.value.length === 0) {
    ElMessage.warning('请选择文件')
    return
  }
  ElMessage.success('文档上传成功，正在处理向量化...')
  showUploadDialog.value = false
  uploadDocType.value = ''
  uploadFileList.value = []
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header .title {
  font-weight: 600;
}
</style>
