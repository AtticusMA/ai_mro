<template>
  <div class="dtwin-layout">
    <!-- Left: 3D Scene -->
    <div class="scene-panel">
      <el-card shadow="never" class="h-full">
        <template #header>
          <div class="panel-header">
            <span class="title">数字孪生机库</span>
            <el-select v-model="selectedHangarId" size="small" style="width:140px" @change="loadHangar">
              <el-option v-for="h in hangars" :key="h.id" :label="h.name" :value="h.id" />
            </el-select>
          </div>
        </template>
        <HangarScene
          ref="sceneRef"
          :model-url="hangarModel?.modelUrl"
          :workstations="workstations"
          @workstation-click="onWorkstationClick"
        />
      </el-card>
    </div>

    <!-- Right: status panel -->
    <div class="info-panel">
      <!-- Legend -->
      <el-card shadow="never" class="mb-3">
        <div class="legend">
          <span v-for="item in STATUS_LEGEND" :key="item.status" class="legend-item">
            <span class="dot" :style="{ background: item.color }" />
            {{ item.label }}
          </span>
        </div>
      </el-card>

      <!-- Workstation list -->
      <el-card shadow="never" class="flex-1">
        <template #header><span class="title">工位状态</span></template>
        <el-scrollbar height="320px">
          <div
            v-for="ws in workstations"
            :key="ws.id"
            class="ws-item"
            :class="{ active: selectedWs?.id === ws.id }"
            @click="onWorkstationClick(ws)"
          >
            <span class="dot" :style="{ background: statusColor(ws.status) }" />
            <span class="ws-name">{{ ws.name }}</span>
            <el-tag size="small" :type="statusTagType(ws.status)" class="ml-auto">
              {{ STATUS_LABEL[ws.status] || ws.status }}
            </el-tag>
            <div v-if="ws.currentAircraftId" class="aircraft-id">{{ ws.currentAircraftId }}</div>
          </div>
        </el-scrollbar>
      </el-card>

      <!-- Selected workstation detail -->
      <el-card v-if="selectedWs" shadow="never" class="mt-3">
        <template #header><span class="title">工位详情</span></template>
        <el-descriptions :column="1" size="small">
          <el-descriptions-item label="工位名称">{{ selectedWs.name }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ STATUS_LABEL[selectedWs.status] }}</el-descriptions-item>
          <el-descriptions-item label="在位飞机">{{ selectedWs.currentAircraftId || '—' }}</el-descriptions-item>
          <el-descriptions-item label="坐标">
            X:{{ selectedWs.positionX }} Y:{{ selectedWs.positionY }} Z:{{ selectedWs.positionZ }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import HangarScene from '@/components/dtwin/HangarScene.vue'
import { getHangarList, getHangarModel, getWorkstations } from '@/api/dtwin'

const STATUS_LEGEND = [
  { status: 'occupied',    color: '#ff6b35', label: '在用' },
  { status: 'idle',        color: '#52c41a', label: '空闲' },
  { status: 'maintenance', color: '#faad14', label: '维护' },
  { status: 'default',     color: '#8c8c8c', label: '未知' }
]

const STATUS_LABEL = { occupied: '在用', idle: '空闲', maintenance: '维护中' }

function statusColor(s) {
  return STATUS_LEGEND.find(x => x.status === s)?.color ?? '#8c8c8c'
}
function statusTagType(s) {
  return { occupied: 'danger', idle: 'success', maintenance: 'warning' }[s] ?? 'info'
}

const sceneRef = ref(null)
const hangars = ref([])
const selectedHangarId = ref(null)
const hangarModel = ref(null)
const workstations = ref([])
const selectedWs = ref(null)
let ws = null   // WebSocket

async function loadHangar(id) {
  if (!id) return
  const [modelRes, wsRes] = await Promise.all([
    getHangarModel(id),
    getWorkstations(id, { pageSize: 100 })
  ])
  if (modelRes.code === 200) hangarModel.value = modelRes.data
  if (wsRes.code === 200) workstations.value = wsRes.data.list
}

function onWorkstationClick(wsObj) {
  selectedWs.value = wsObj
}

function connectWs() {
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  const url = `${proto}://${location.host}/ws/dtwin`
  ws = new WebSocket(url)
  ws.onmessage = (evt) => {
    try {
      const msg = JSON.parse(evt.data)
      if (msg.type === 'workstation_change') {
        const idx = workstations.value.findIndex(w => w.id === msg.workstationId)
        if (idx !== -1) {
          workstations.value[idx] = { ...workstations.value[idx], status: msg.status, currentAircraftId: msg.aircraftId }
          sceneRef.value?.updateWorkstationStatus(msg.workstationId, msg.status)
        }
      }
    } catch { /* ignore malformed */ }
  }
  ws.onclose = () => setTimeout(connectWs, 3000)
}

onMounted(async () => {
  const res = await getHangarList()
  if (res.code === 200 && res.data.list.length) {
    hangars.value = res.data.list
    selectedHangarId.value = res.data.list[0].id
    await loadHangar(selectedHangarId.value)
  }
  // Only connect WS in non-mock environment
  if (import.meta.env.VITE_USE_MOCK !== 'true') connectWs()
})

onUnmounted(() => {
  ws?.close()
})
</script>

<style scoped>
.dtwin-layout {
  display: flex;
  gap: 12px;
  height: calc(100vh - 140px);
}
.scene-panel {
  flex: 1;
  min-width: 0;
}
.scene-panel :deep(.el-card__body) {
  padding: 8px;
  height: calc(100% - 52px);
}
.info-panel {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title { font-weight: 600; }
.legend { display: flex; flex-wrap: wrap; gap: 10px; }
.legend-item { display: flex; align-items: center; gap: 4px; font-size: 12px; }
.dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.ws-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  flex-wrap: wrap;
}
.ws-item:hover { background: var(--el-fill-color); }
.ws-item.active { background: var(--el-color-primary-light-9); }
.ws-name { flex: 1; font-size: 13px; }
.aircraft-id { width: 100%; font-size: 11px; color: var(--el-text-color-secondary); padding-left: 16px; }
.flex-1 { flex: 1; }
.h-full { height: 100%; }
.mb-3 { margin-bottom: 12px; }
.mt-3 { margin-top: 12px; }
.ml-auto { margin-left: auto; }
</style>
