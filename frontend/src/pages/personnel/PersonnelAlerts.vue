<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElTable, ElTableColumn, ElButton, ElTag, ElCard, ElRow, ElCol, ElLoading, ElPagination } from 'element-plus';
import { getLicenseAlerts } from '@/api/personnel.js';

// State
const alerts = ref([]);
const loading = ref(false);
const activeFilter = ref('all');
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);

// Computed properties
const filteredList = computed(() => {
  if (activeFilter.value === 'all') return alerts.value;
  
  return alerts.value.filter(alert => alert.status === activeFilter.value);
});

const expiredCount = computed(() => 
  alerts.value.filter(alert => alert.status === 'expired').length
);

const criticalCount = computed(() => 
  alerts.value.filter(alert => alert.status === 'critical').length
);

const warningCount = computed(() => 
  alerts.value.filter(alert => alert.status === 'warning').length
);

const noticeCount = computed(() => 
  alerts.value.filter(alert => alert.status === 'notice').length
);

// Methods
const loadAlerts = async () => {
  loading.value = true;
  try {
    const response = await getLicenseAlerts({
      page: currentPage.value,
      size: pageSize.value
    });
    
    alerts.value = response.data?.list || [];
    total.value = response.data?.total || 0;
  } catch (error) {
    console.error('Failed to load license alerts:', error);
  } finally {
    loading.value = false;
  }
};

const handleFilterChange = (filter) => {
  activeFilter.value = filter;
  currentPage.value = 1;
};

const getStatusColor = (status) => {
  switch (status) {
    case 'expired': return 'danger';
    case 'critical': return 'warning';
    case 'warning': return 'warning';
    case 'notice': return 'info';
    default: return 'info';
  }
};

const getDaysText = (alert) => {
  if (alert.status === 'expired') {
    return `已过期 ${Math.abs(alert.days_remaining)}天`;
  } else {
    return `还剩 ${alert.days_remaining} 天`;
  }
};

const getDaysColor = (alert) => {
  switch (alert.status) {
    case 'expired': return 'text-red-600';
    case 'critical': return 'text-orange-600';
    case 'warning': return 'text-yellow-600';
    case 'notice': return 'text-blue-600';
    default: return 'text-gray-600';
  }
};

// Lifecycle
onMounted(() => {
  loadAlerts();
});

// Pagination handler
const handlePageChange = (newPage) => {
  currentPage.value = newPage;
  loadAlerts();
};
</script>

<template>
  <div class="p-4 md:p-6">
    <!-- Page Header -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800">证照到期预警</h1>
      <ElButton type="primary" @click="$router.push('/mro/personnel')">
        <i class="el-icon-back mr-2"></i> 返回人员管理
      </ElButton>
    </div>

    <!-- Stat Cards -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
      <!-- 已过期 -->
      <ElCard class="bg-red-50 border-red-200 border rounded-lg shadow-sm">
        <div class="flex flex-col items-center p-4">
          <div class="text-3xl font-bold text-red-700">{{ expiredCount }}</div>
          <div class="text-sm text-red-600 mt-1">已过期</div>
        </div>
      </ElCard>
      
      <!-- 紧急(30天内) -->
      <ElCard class="bg-orange-50 border-orange-200 border rounded-lg shadow-sm">
        <div class="flex flex-col items-center p-4">
          <div class="text-3xl font-bold text-orange-700">{{ criticalCount }}</div>
          <div class="text-sm text-orange-600 mt-1">紧急(30天内)</div>
        </div>
      </ElCard>
      
      <!-- 预警(31-60天) -->
      <ElCard class="bg-yellow-50 border-yellow-200 border rounded-lg shadow-sm">
        <div class="flex flex-col items-center p-4">
          <div class="text-3xl font-bold text-yellow-700">{{ warningCount }}</div>
          <div class="text-sm text-yellow-600 mt-1">预警(31-60天)</div>
        </div>
      </ElCard>
      
      <!-- 关注(61-90天) -->
      <ElCard class="bg-blue-50 border-blue-200 border rounded-lg shadow-sm">
        <div class="flex flex-col items-center p-4">
          <div class="text-3xl font-bold text-blue-700">{{ noticeCount }}</div>
          <div class="text-sm text-blue-600 mt-1">关注(61-90天)</div>
        </div>
      </ElCard>
    </div>

    <!-- Filter Bar -->
    <div class="mb-6">
      <div class="inline-flex rounded-md shadow-sm" role="group">
        <button 
          @click="handleFilterChange('all')"
          :class="['px-4 py-2 text-sm font-medium rounded-l-lg', activeFilter === 'all' ? 'bg-blue-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-100']"
        >
          全部
        </button>
        <button 
          @click="handleFilterChange('expired')"
          :class="['px-4 py-2 text-sm font-medium', activeFilter === 'expired' ? 'bg-red-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-100']"
        >
          已过期
        </button>
        <button 
          @click="handleFilterChange('critical')"
          :class="['px-4 py-2 text-sm font-medium', activeFilter === 'critical' ? 'bg-orange-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-100']"
        >
          紧急
        </button>
        <button 
          @click="handleFilterChange('warning')"
          :class="['px-4 py-2 text-sm font-medium', activeFilter === 'warning' ? 'bg-yellow-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-100']"
        >
          预警
        </button>
        <button 
          @click="handleFilterChange('notice')"
          :class="['px-4 py-2 text-sm font-medium rounded-r-lg', activeFilter === 'notice' ? 'bg-blue-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-100']"
        >
          关注
        </button>
      </div>
    </div>

    <!-- Alert List -->
    <div class="mb-6">
      <div v-loading="loading" element-loading-text="正在加载..." class="rounded-lg border">
        <!-- Desktop Table View -->
        <div class="hidden sm:block">
          <ElTable 
            :data="filteredList" 
            style="width: 100%" 
            stripe
            :default-sort="{ prop: 'days_remaining', order: 'ascending' }"
          >
            <ElTableColumn prop="personnel_name" label="人员姓名 + 工号" width="200">
              <template #default="scope">
                <div class="font-medium">{{ scope.row.personnel_name }}</div>
                <div class="text-sm text-gray-500">{{ scope.row.employee_no }}</div>
              </template>
            </ElTableColumn>
            
            <ElTableColumn prop="license_name" label="证件名称" width="150" />
            
            <ElTableColumn prop="license_no" label="证件编号" width="150" />
            
            <ElTableColumn prop="expiry_date" label="到期日期" width="120">
              <template #default="scope">
                {{ scope.row.expiry_date }}
              </template>
            </ElTableColumn>
            
            <ElTableColumn prop="days_remaining" label="剩余天数" width="120" sortable>
              <template #default="scope">
                <span :class="getDaysColor(scope.row)">{{ getDaysText(scope.row) }}</span>
              </template>
            </ElTableColumn>
            
            <ElTableColumn prop="status" label="状态" width="100">
              <template #default="scope">
                <ElTag :type="getStatusColor(scope.row.status)">
                  {{ scope.row.status === 'expired' ? '已过期' : 
                     scope.row.status === 'critical' ? '紧急' : 
                     scope.row.status === 'warning' ? '预警' : '关注' }}
                </ElTag>
              </template>
            </ElTableColumn>
            
            <ElTableColumn label="操作" width="120">
              <template #default="scope">
                <ElButton 
                  size="small" 
                  type="primary" 
                  @click="$router.push(`/mro/personnel/${scope.row.personnel_id}`)"
                >
                  查看人员
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
        
        <!-- Mobile Card View -->
        <div class="sm:hidden">
          <div v-for="alert in filteredList" :key="alert.id" class="border rounded-lg p-4 mb-3 bg-white shadow-sm">
            <div class="flex justify-between items-start">
              <div>
                <h3 class="font-bold text-lg">{{ alert.personnel_name }} ({{ alert.employee_no }})</h3>
                <p class="text-gray-600 mt-1">{{ alert.license_name }} - {{ alert.license_no }}</p>
              </div>
              <ElTag :type="getStatusColor(alert.status)" class="ml-2">
                {{ alert.status === 'expired' ? '已过期' : 
                   alert.status === 'critical' ? '紧急' : 
                   alert.status === 'warning' ? '预警' : '关注' }}
              </ElTag>
            </div>
            
            <div class="mt-3 grid grid-cols-2 gap-2">
              <div>
                <span class="text-sm text-gray-500">到期日期</span>
                <p class="font-medium">{{ alert.expiry_date }}</p>
              </div>
              <div>
                <span class="text-sm text-gray-500">剩余天数</span>
                <p :class="getDaysColor(alert)">{{ getDaysText(alert) }}</p>
              </div>
            </div>
            
            <div class="mt-4">
              <ElButton 
                size="small" 
                type="primary" 
                @click="$router.push(`/mro/personnel/${alert.personnel_id}`)"
              >
                查看人员
              </ElButton>
            </div>
          </div>
          
          <div v-if="filteredList.length === 0" class="text-center py-8 text-gray-500">
            暂无符合条件的预警信息
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="flex justify-center">
      <ElPagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
/* Add responsive styles */
@media (max-width: 768px) {
  .el-table .cell {
    word-break: break-word;
  }
}
</style>