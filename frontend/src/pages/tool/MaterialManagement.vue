<template>
  <div class="material-management">
    <el-row :gutter="16">
      <!-- Left: Material Inventory -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>航材库存</span>
              <div class="actions">
                <el-checkbox v-model="materialQuery.lowStock" label="仅显示低库存" @change="loadMaterials" />
                <el-button type="primary" size="small" style="margin-left:12px" @click="openCreateDialog">新增入库</el-button>
              </div>
            </div>
          </template>
          <el-table :data="materialList" size="small" stripe>
            <el-table-column label="件号" prop="partNo" width="150" />
            <el-table-column label="航材名称" prop="name" min-width="140" />
            <el-table-column label="分类" prop="category" width="90" />
            <el-table-column label="库存" width="80" align="center">
              <template #default="{ row }">
                <span :class="row.belowMinStock ? 'text-danger' : ''">{{ row.stockQty }}</span>
              </template>
            </el-table-column>
            <el-table-column label="最低库存" prop="minStock" width="80" align="center" />
            <el-table-column label="存放位置" prop="location" width="110" />
            <el-table-column label="有效期" prop="expiryDate" width="110" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.belowMinStock ? 'danger' : 'success'" size="small">
                  {{ row.belowMinStock ? '低库存' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link @click="openRepairDialog(row)">送修</el-button>
                <el-button size="small" link type="primary" @click="openEditDialog(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="materialQuery.pageNum"
            v-model:page-size="materialQuery.pageSize"
            :total="materialTotal"
            layout="total, prev, pager, next"
            style="margin-top:12px"
            @current-change="loadMaterials"
          />
        </el-card>
      </el-col>

      <!-- Right: Restock Alerts + Repair Orders -->
      <el-col :span="8">
        <el-card style="margin-bottom:16px">
          <template #header><span>补货预警</span></template>
          <div v-for="alert in restockAlerts" :key="alert.id" class="alert-item">
            <div class="alert-name">{{ alert.name }}</div>
            <div class="alert-info">
              <span class="part-no">{{ alert.partNo }}</span>
              <div>
                <span class="text-danger">现存: {{ alert.stockQty }}</span> /
                <span>最低: {{ alert.minStock }}</span>
              </div>
              <span class="location">{{ alert.location }}</span>
            </div>
          </div>
          <el-empty v-if="restockAlerts.length === 0" description="库存充足" :image-size="40" />
        </el-card>

        <el-card>
          <template #header>
            <div class="card-header">
              <span>送修单</span>
            </div>
          </template>
          <div v-for="order in repairOrders" :key="order.id" class="order-item">
            <div class="order-name">{{ order.materialName }}</div>
            <div class="order-info">
              <span>{{ order.partNo }}</span>
              <el-tag :type="repairStatusType(order.status)" size="small">{{ repairStatusLabel(order.status) }}</el-tag>
            </div>
          </div>
          <el-empty v-if="repairOrders.length === 0" description="暂无送修单" :image-size="40" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Create/Edit Material Dialog -->
    <el-dialog v-model="materialDialogVisible" :title="editingMaterial?.id ? '编辑航材' : '新增航材入库'" width="560px">
      <el-form :model="materialForm" label-width="90px" size="small">
        <el-form-item label="件号" required>
          <el-input v-model="materialForm.partNo" :disabled="!!editingMaterial?.id" />
        </el-form-item>
        <el-form-item label="航材名称" required>
          <el-input v-model="materialForm.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="materialForm.category" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="库存数量">
              <el-input-number v-model="materialForm.stockQty" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低库存">
              <el-input-number v-model="materialForm.minStock" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="存放位置">
          <el-input v-model="materialForm.location" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker v-model="materialForm.expiryDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="materialDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMaterial">保存</el-button>
      </template>
    </el-dialog>

    <!-- Repair Order Dialog -->
    <el-dialog v-model="repairDialogVisible" title="创建送修单" width="480px">
      <el-form :model="repairForm" label-width="90px" size="small">
        <el-form-item label="航材">
          <el-input :value="repairForm.materialName" disabled />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="repairForm.quantity" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="故障描述">
          <el-input v-model="repairForm.faultDescription" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="repairDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRepairOrder">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listMaterials, createMaterial, updateMaterial, listMaterialAlerts, listRepairOrders, createRepairOrder } from '@/api/tool'

const materialList = ref([])
const materialTotal = ref(0)
const restockAlerts = ref([])
const repairOrders = ref([])

const materialQuery = ref({ pageNum: 1, pageSize: 20, lowStock: false })
const materialDialogVisible = ref(false)
const editingMaterial = ref(null)
const materialForm = ref({ partNo: '', name: '', category: '', stockQty: 0, minStock: 0, location: '', expiryDate: null })

const repairDialogVisible = ref(false)
const repairForm = ref({ materialId: null, materialName: '', quantity: 1, faultDescription: '' })

const repairStatusLabel = (s) => ({ pending: '待审批', in_repair: '维修中', nff: 'NFF', completed: '已完成' }[s] || s)
const repairStatusType = (s) => ({ pending: 'warning', in_repair: '', nff: 'info', completed: 'success' }[s] || '')

async function loadMaterials() {
  const params = { pageNum: materialQuery.value.pageNum, pageSize: materialQuery.value.pageSize }
  if (materialQuery.value.lowStock) params.lowStock = true
  const res = await listMaterials(params)
  materialList.value = res.data?.list || []
  materialTotal.value = res.data?.total || 0
}

async function loadRestockAlerts() {
  const res = await listMaterialAlerts({ pageNum: 1, pageSize: 10 })
  restockAlerts.value = res.data?.list || []
}

async function loadRepairOrders() {
  const res = await listRepairOrders({ pageNum: 1, pageSize: 5 })
  repairOrders.value = res.data?.list || []
}

function openCreateDialog() {
  editingMaterial.value = null
  materialForm.value = { partNo: '', name: '', category: '', stockQty: 0, minStock: 0, location: '', expiryDate: null }
  materialDialogVisible.value = true
}

function openEditDialog(row) {
  editingMaterial.value = row
  materialForm.value = { ...row }
  materialDialogVisible.value = true
}

async function saveMaterial() {
  if (editingMaterial.value?.id) {
    await updateMaterial(editingMaterial.value.id, materialForm.value)
    ElMessage.success('更新成功')
  } else {
    await createMaterial(materialForm.value)
    ElMessage.success('入库成功')
  }
  materialDialogVisible.value = false
  loadMaterials()
  loadRestockAlerts()
}

function openRepairDialog(row) {
  repairForm.value = { materialId: row.id, materialName: row.name, quantity: 1, faultDescription: '' }
  repairDialogVisible.value = true
}

async function submitRepairOrder() {
  await createRepairOrder({
    materialId: repairForm.value.materialId,
    quantity: repairForm.value.quantity,
    faultDescription: repairForm.value.faultDescription,
    vendorId: null
  })
  ElMessage.success('送修单创建成功')
  repairDialogVisible.value = false
  loadRepairOrders()
}

onMounted(() => {
  loadMaterials()
  loadRestockAlerts()
  loadRepairOrders()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.actions { display: flex; align-items: center; }
.alert-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.alert-item:last-child { border-bottom: none; }
.alert-name { font-weight: 500; }
.alert-info { font-size: 12px; color: #606266; margin-top: 4px; display: flex; flex-direction: column; gap: 2px; }
.part-no { color: #909399; }
.location { color: #909399; }
.order-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.order-item:last-child { border-bottom: none; }
.order-name { font-weight: 500; }
.order-info { font-size: 12px; display: flex; justify-content: space-between; align-items: center; margin-top: 4px; }
.text-danger { color: #f56c6c; }
</style>
