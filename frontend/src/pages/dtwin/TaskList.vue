<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElTable, ElTableColumn, ElPagination, ElButton, ElInput, ElSelect, ElOption, ElTag, ElProgress, ElDatePicker, ElDialog, ElFormItem, ElForm, ElPopconfirm } from 'element-plus';
import { getTaskList, createTask, deleteTask } from '@/api/planning.js';

// State
const taskList = ref([]);
const total = ref(0);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const isMobile = ref(window.innerWidth < 768);

// Filters
const aircraftReg = ref('');
const checkType = ref('');
const statusFilter = ref('');

// Create dialog
const dialogVisible = ref(false);
const createForm = ref({
  aircraft_reg: '',
  aircraft_type: '',
  check_type: '',
  planned_start: '',
  planned_end: ''
});

// Status mapping
const statusMap = {
  'planning': { label: '计划中', type: 'info' },
  'in_progress': { label: '进行中', type: 'warning' },
  'completed': { label: '已完成', type: 'success' }
};

// Load task list
const loadTaskList = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      aircraft_reg: aircraftReg.value,
      check_type: checkType.value,
      status: statusFilter.value
    };
    
    const response = await getTaskList(params);
    taskList.value = response.data.list || [];
    total.value = response.data.total || 0;
  } catch (error) {
    ElMessage.error('获取任务列表失败：' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// Handle pagination change
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  loadTaskList();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadTaskList();
};

// Filter functions
const applyFilters = () => {
  currentPage.value = 1;
  loadTaskList();
};

const resetFilters = () => {
  aircraftReg.value = '';
  checkType.value = '';
  statusFilter.value = '';
  currentPage.value = 1;
  loadTaskList();
};

// Create task
const handleCreate = async () => {
  try {
    await createTask(createForm.value);
    ElMessage.success('新建任务包成功');
    dialogVisible.value = false;
    // Reset form
    createForm.value = {
      aircraft_reg: '',
      aircraft_type: '',
      check_type: '',
      planned_start: '',
      planned_end: ''
    };
    loadTaskList();
  } catch (error) {
    ElMessage.error('新建任务包失败：' + (error.message || '未知错误'));
  }
};

// Delete task
const handleDelete = async (id) => {
  try {
    await deleteTask(id);
    ElMessage.success('删除成功');
    loadTaskList();
  } catch (error) {
    ElMessage.error('删除失败：' + (error.message || '未知错误'));
  }
};

// Mobile detection
const handleResize = () => {
  isMobile.value = window.innerWidth < 768;
};

onMounted(() => {
  loadTaskList();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<template>
  <div class="p-4">
    <!-- Page Header -->
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">维修任务包管理</h1>
      <ElButton type="primary" @click="dialogVisible = true">+ 新建任务包</ElButton>
    </div>

    <!-- Filter Bar -->
    <div class="bg-white p-4 rounded-lg shadow-sm mb-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <!-- Aircraft registration search -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">注册号</label>
          <ElInput v-model="aircraftReg" placeholder="请输入注册号" clearable />
        </div>
        
        <!-- Check type filter -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">检修类型</label>
          <ElSelect v-model="checkType" placeholder="全部" clearable>
            <ElOption label="C检" value="C检" />
            <ElOption label="D检" value="D检" />
            <ElOption label="定检" value="定检" />
          </ElSelect>
        </div>
        
        <!-- Status filter pills -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">状态</label>
          <div class="flex flex-wrap gap-2">
            <ElButton 
              :type="statusFilter === '' ? 'primary' : 'default'"
              size="small"
              @click="statusFilter = ''; applyFilters()"
            >
              全部
            </ElButton>
            <ElButton 
              :type="statusFilter === 'planning' ? 'primary' : 'default'"
              size="small"
              @click="statusFilter = 'planning'; applyFilters()"
            >
              计划中
            </ElButton>
            <ElButton 
              :type="statusFilter === 'in_progress' ? 'primary' : 'default'"
              size="small"
              @click="statusFilter = 'in_progress'; applyFilters()"
            >
              进行中
            </ElButton>
            <ElButton 
              :type="statusFilter === 'completed' ? 'primary' : 'default'"
              size="small"
              @click="statusFilter = 'completed'; applyFilters()"
            >
              已完成
            </ElButton>
          </div>
        </div>
      </div>
      
      <div class="mt-4 flex justify-end">
        <ElButton @click="resetFilters" size="small">重置</ElButton>
        <ElButton type="primary" @click="applyFilters" class="ml-2" size="small">搜索</ElButton>
      </div>
    </div>

    <!-- Task List -->
    <div v-if="!isMobile" class="bg-white rounded-lg shadow-sm overflow-hidden">
      <ElTable 
        :data="taskList" 
        v-loading="loading" 
        style="width: 100%"
        class="border rounded-lg"
      >
        <ElTableColumn prop="task_no" label="任务编号" width="120" />
        
        <ElTableColumn label="注册号 / 机型" width="160">
          <template #default="scope">
            <div class="font-medium">{{ scope.row.aircraft_reg }}</div>
            <div class="text-gray-500 text-sm">{{ scope.row.aircraft_type }}</div>
          </template>
        </ElTableColumn>
        
        <ElTableColumn prop="check_type" label="检修类型" width="100" />
        
        <ElTableColumn label="计划周期" width="180">
          <template #default="scope">
            {{ scope.row.planned_start }} ~ {{ scope.row.planned_end }}
          </template>
        </ElTableColumn>
        
        <ElTableColumn label="进度" width="200">
          <template #default="scope">
            <ElProgress 
              :percentage="scope.row.completed_count && scope.row.workcard_count 
                ? Math.round((scope.row.completed_count / scope.row.workcard_count) * 100) 
                : 0"
              :stroke-width="20"
              :show-text="true"
            />
          </template>
        </ElTableColumn>
        
        <ElTableColumn label="状态" width="100">
          <template #default="scope">
            <ElTag 
              :type="statusMap[scope.row.status]?.type || 'info'"
              size="small"
            >
              {{ statusMap[scope.row.status]?.label || scope.row.status }}
            </ElTag>
          </template>
        </ElTableColumn>
        
        <ElTableColumn label="操作" width="180">
          <template #default="scope">
            <ElButton 
              size="small" 
              type="text" 
              @click="$router.push(`/mro/planning/tasks/${scope.row.id}`)"
            >
              查看详情
            </ElButton>
            <ElPopconfirm 
              title="确定要删除此任务包吗？" 
              @confirm="() => handleDelete(scope.row.id)"
              width="200"
            >
              <template #reference>
                <ElButton size="small" type="text" class="text-red-500">删除</ElButton>
              </template>
            </ElPopconfirm>
          </template>
        </ElTableColumn>
      </ElTable>
      
      <!-- Pagination -->
      <div class="flex justify-end p-4 border-t">
        <ElPagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </div>

    <!-- Mobile Cards -->
    <div v-else class="space-y-4">
      <div 
        v-for="task in taskList" 
        :key="task.id" 
        class="bg-white rounded-lg shadow-sm p-4 border"
      >
        <div class="flex justify-between items-start">
          <div>
            <h3 class="font-bold text-lg">{{ task.task_no }}</h3>
            <div class="mt-1 text-gray-600">
              <span class="font-medium">{{ task.aircraft_reg }}</span>
              <span class="mx-2">/</span>
              <span>{{ task.aircraft_type }}</span>
            </div>
          </div>
          <ElTag 
            :type="statusMap[task.status]?.type || 'info'"
            size="small"
          >
            {{ statusMap[task.status]?.label || task.status }}
          </ElTag>
        </div>
        
        <div class="mt-3">
          <div class="text-sm text-gray-600">检修类型：{{ task.check_type }}</div>
          <div class="text-sm text-gray-600 mt-1">
            计划周期：{{ task.planned_start }} ~ {{ task.planned_end }}
          </div>
        </div>
        
        <div class="mt-4">
          <div class="text-sm font-medium mb-1">进度</div>
          <ElProgress 
            :percentage="task.completed_count && task.workcard_count 
              ? Math.round((task.completed_count / task.workcard_count) * 100) 
              : 0"
            :stroke-width="16"
            :show-text="true"
          />
        </div>
        
        <div class="mt-4 flex space-x-2">
          <ElButton 
            size="small" 
            type="primary" 
            @click="$router.push(`/mro/planning/tasks/${task.id}`)"
          >
            查看详情
          </ElButton>
          <ElPopconfirm 
            title="确定要删除此任务包吗？" 
            @confirm="() => handleDelete(task.id)"
            width="200"
          >
            <template #reference>
              <ElButton size="small" type="danger">删除</ElButton>
            </template>
          </ElPopconfirm>
        </div>
      </div>
      
      <div v-if="taskList.length === 0" class="text-center py-8 text-gray-500">
        暂无任务包
      </div>
    </div>

    <!-- Create Task Dialog -->
    <ElDialog 
      v-model="dialogVisible" 
      title="新建任务包" 
      width="500px"
      @close="() => {
        createForm.value = {
          aircraft_reg: '',
          aircraft_type: '',
          check_type: '',
          planned_start: '',
          planned_end: ''
        }
      }"
    >
      <ElForm :model="createForm" label-width="100px" class="max-w-md">
        <ElFormItem label="注册号*" required>
          <ElInput v-model="createForm.aircraft_reg" placeholder="请输入注册号" />
        </ElFormItem>
        
        <ElFormItem label="机型*" required>
          <ElInput v-model="createForm.aircraft_type" placeholder="请输入机型" />
        </ElFormItem>
        
        <ElFormItem label="检修类型*" required>
          <ElSelect v-model="createForm.check_type" placeholder="请选择" class="w-full">
            <ElOption label="C检" value="C检" />
            <ElOption label="D检" value="D检" />
            <ElOption label="定检" value="定检" />
          </ElSelect>
        </ElFormItem>
        
        <ElFormItem label="计划开始日期*" required>
          <ElDatePicker
            v-model="createForm.planned_start"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        
        <ElFormItem label="计划结束日期*" required>
          <ElDatePicker
            v-model="createForm.planned_end"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
      </ElForm>
      
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="handleCreate">确认</ElButton>
        </span>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
/* Mobile responsive adjustments */
@media (max-width: 767px) {
  .p-4 {
    padding: 1rem;
  }
  
  h1 {
    font-size: 1.5rem;
  }
  
  .el-table .el-table__body-wrapper {
    max-height: none;
  }
}
</style>