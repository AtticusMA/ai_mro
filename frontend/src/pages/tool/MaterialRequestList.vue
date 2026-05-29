<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getMaterialRequestList, createMaterialRequest } from '@/api/material-request';

// State
const loading = ref(false);
const materialRequests = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// Filters
const statusFilter = ref('all');
const urgencyFilter = ref('all');
const workcardSearch = ref('');

// Dialog state
const dialogVisible = ref(false);
const newRequest = ref({
  workcard_no: '',
  title: '',
  urgency: 'normal',
  description: ''
});

// Status mapping
const statusMap = {
  pending_approval: { label: '待审批', type: 'warning' },
  approved: { label: '已批准', type: 'success' },
  rejected: { label: '已拒绝', type: 'danger' },
  delivered: { label: '已发料', type: 'primary' },
  received: { label: '已接收', type: 'success' }
};

const urgencyMap = {
  high: { label: '紧急', type: 'danger' },
  normal: { label: '普通', type: 'warning' },
  low: { label: '低优先', type: 'info' }
};

// Computed properties
const filteredRequests = computed(() => {
  return materialRequests.value.filter(request => {
    const statusMatch = statusFilter.value === 'all' || request.status === statusFilter.value;
    const urgencyMatch = urgencyFilter.value === 'all' || request.urgency === urgencyFilter.value;
    const workcardMatch = !workcardSearch.value || 
      request.workcard_no?.includes(workcardSearch.value) || 
      request.request_no?.includes(workcardSearch.value);
    
    return statusMatch && urgencyMatch && workcardMatch;
  });
});

// Methods
const fetchMaterialRequests = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: statusFilter.value === 'all' ? undefined : statusFilter.value,
      urgency: urgencyFilter.value === 'all' ? undefined : urgencyFilter.value,
      workcard_no: workcardSearch.value || undefined
    };
    
    const response = await getMaterialRequestList(params);
    materialRequests.value = response.data.list || [];
    total.value = response.data.total || 0;
  } catch (error) {
    ElMessage.error('获取航材申请列表失败：' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};

const handleCreateRequest = async () => {
  if (!newRequest.value.title.trim()) {
    ElMessage.warning('请填写申请标题');
    return;
  }
  
  try {
    await createMaterialRequest(newRequest.value);
    ElMessage.success('新建申请成功');
    dialogVisible.value = false;
    newRequest.value = {
      workcard_no: '',
      title: '',
      urgency: 'normal',
      description: ''
    };
    fetchMaterialRequests();
  } catch (error) {
    ElMessage.error('创建申请失败：' + (error.response?.data?.message || error.message));
  }
};

const handleStatusChange = (status) => {
  statusFilter.value = status;
  currentPage.value = 1;
  fetchMaterialRequests();
};

const handleUrgencyChange = (urgency) => {
  urgencyFilter.value = urgency;
  currentPage.value = 1;
  fetchMaterialRequests();
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchMaterialRequests();
};

const handleResetFilters = () => {
  statusFilter.value = 'all';
  urgencyFilter.value = 'all';
  workcardSearch.value = '';
  currentPage.value = 1;
  fetchMaterialRequests();
};

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchMaterialRequests();
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchMaterialRequests();
};

// Initialize
onMounted(() => {
  fetchMaterialRequests();
});
</script>

<template>
  <div class="p-4">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-4 sm:mb-0">航材申请管理</h1>
      <div>
        <el-button type="primary" @click="dialogVisible = true">
          + 新建申请
        </el-button>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <!-- Status Filter Pills -->
        <div class="flex flex-wrap gap-2">
          <el-tag 
            :type="statusFilter === 'all' ? 'primary' : ''" 
            :effect="statusFilter === 'all' ? 'dark' : 'plain'"
            @click="handleStatusChange('all')"
            class="cursor-pointer"
          >
            全部
          </el-tag>
          <el-tag 
            :type="statusFilter === 'pending_approval' ? 'warning' : ''" 
            :effect="statusFilter === 'pending_approval' ? 'dark' : 'plain'"
            @click="handleStatusChange('pending_approval')"
            class="cursor-pointer"
          >
            待审批
          </el-tag>
          <el-tag 
            :type="statusFilter === 'approved' ? 'success' : ''" 
            :effect="statusFilter === 'approved' ? 'dark' : 'plain'"
            @click="handleStatusChange('approved')"
            class="cursor-pointer"
          >
            已批准
          </el-tag>
          <el-tag 
            :type="statusFilter === 'rejected' ? 'danger' : ''" 
            :effect="statusFilter === 'rejected' ? 'dark' : 'plain'"
            @click="handleStatusChange('rejected')"
            class="cursor-pointer"
          >
            已拒绝
          </el-tag>
          <el-tag 
            :type="statusFilter === 'delivered' ? 'primary' : ''" 
            :effect="statusFilter === 'delivered' ? 'dark' : 'plain'"
            @click="handleStatusChange('delivered')"
            class="cursor-pointer"
          >
            已发料
          </el-tag>
          <el-tag 
            :type="statusFilter === 'received' ? 'success' : ''" 
            :effect="statusFilter === 'received' ? 'dark' : 'plain'"
            @click="handleStatusChange('received')"
            class="cursor-pointer"
          >
            已接收
          </el-tag>
        </div>
        
        <!-- Urgency Filter and Workcard Search -->
        <div class="flex flex-col sm:flex-row gap-3 w-full md:w-auto">
          <el-select 
            v-model="urgencyFilter" 
            placeholder="紧急程度" 
            class="w-full sm:w-40"
            @change="handleUrgencyChange"
          >
            <el-option label="全部" value="all"></el-option>
            <el-option label="紧急" value="high"></el-option>
            <el-option label="普通" value="normal"></el-option>
            <el-option label="低优先" value="low"></el-option>
          </el-select>
          
          <div class="relative w-full sm:w-64">
            <el-input 
              v-model="workcardSearch" 
              placeholder="搜索工卡号/申请编号" 
              @keyup.enter="handleSearch"
              class="w-full"
            >
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>
          
          <el-button @click="handleResetFilters" plain>重置</el-button>
        </div>
      </div>
    </div>

    <!-- Mobile View (Cards) -->
    <div class="md:hidden">
      <div v-if="loading" class="text-center py-8">
        <el-skeleton style="width: 100%" :rows="3" />
      </div>
      
      <div v-else-if="filteredRequests.length === 0" class="text-center py-8 text-gray-500">
        暂无航材申请记录
      </div>
      
      <div v-else class="space-y-4">
        <div 
          v-for="request in filteredRequests" 
          :key="request.id" 
          class="bg-white rounded-lg shadow-sm p-4 border border-gray-100"
        >
          <div class="flex justify-between items-start mb-2">
            <div>
              <h3 class="font-semibold text-gray-800">{{ request.request_no }}</h3>
              <p class="text-sm text-gray-600 mt-1">{{ request.title }}</p>
            </div>
            <div class="flex gap-2">
              <el-tag 
                :type="statusMap[request.status]?.type || 'primary'" 
                size="small"
              >
                {{ statusMap[request.status]?.label || request.status }}
              </el-tag>
              <el-tag 
                :type="urgencyMap[request.urgency]?.type || 'info'" 
                size="small"
              >
                {{ urgencyMap[request.urgency]?.label || request.urgency }}
              </el-tag>
            </div>
          </div>
          
          <div class="grid grid-cols-2 gap-2 text-sm mt-3">
            <div>
              <span class="text-gray-500">关联工卡：</span>
              <span class="font-medium">{{ request.workcard_no }}</span>
            </div>
            <div>
              <span class="text-gray-500">申请时间：</span>
              <span class="font-medium">{{ request.requested_at }}</span>
            </div>
          </div>
          
          <div class="mt-4 flex justify-end">
            <el-button 
              type="primary" 
              size="small" 
              @click="$router.push(`/mro/material-request/${request.id}`)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Desktop View (Table) -->
    <div class="hidden md:block">
      <el-table 
        :data="filteredRequests" 
        stripe 
        style="width: 100%" 
        :loading="loading"
      >
        <el-table-column prop="request_no" label="申请编号" width="120" />
        
        <el-table-column label="关联工卡" width="120">
          <template #default="{ row }">
            <el-tag type="primary" size="small" effect="plain">
              {{ row.workcard_no }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="title" label="申请标题" />
        
        <el-table-column label="申请人+部门" width="180">
          <template #default="{ row }">
            <div>{{ row.requester }}</div>
            <div class="text-xs text-gray-500">{{ row.department }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="紧急程度" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="urgencyMap[row.urgency]?.type || 'info'" 
              size="small"
            >
              {{ urgencyMap[row.urgency]?.label || row.urgency }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="statusMap[row.status]?.type || 'primary'" 
              size="small"
            >
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="requested_at" label="申请时间" width="160" />
        
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button 
              type="text" 
              size="small" 
              @click="$router.push(`/mro/material-request/${row.id}`)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pagination -->
    <div class="mt-6 flex justify-center">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>

  <!-- Create Request Dialog -->
  <el-dialog
    v-model="dialogVisible"
    title="新建航材申请"
    width="50%"
    :close-on-click-modal="false"
  >
    <el-form :model="newRequest" label-width="100px" class="max-w-2xl">
      <el-form-item label="关联工卡">
        <el-input v-model="newRequest.workcard_no" placeholder="请输入工卡号" />
      </el-form-item>
      
      <el-form-item label="申请标题" required>
        <el-input v-model="newRequest.title" placeholder="请输入申请标题" />
      </el-form-item>
      
      <el-form-item label="紧急程度">
        <el-select v-model="newRequest.urgency" class="w-full">
          <el-option label="紧急" value="high"></el-option>
          <el-option label="普通" value="normal"></el-option>
          <el-option label="低优先" value="low"></el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="申请说明">
        <el-input 
          v-model="newRequest.description" 
          type="textarea" 
          :rows="4" 
          placeholder="请输入申请说明（可选）" 
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateRequest">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
/* Add responsive styles */
@media (max-width: 768px) {
  .p-4 {
    padding: 1rem;
  }
  
  .text-2xl {
    font-size: 1.5rem;
  }
  
  .el-table :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }
}
</style>