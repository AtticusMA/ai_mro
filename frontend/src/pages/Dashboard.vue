<template>
  <div class="ops">
    <!-- ═══════════ 状态总览条 ═══════════ -->
    <section class="ops-hero">
      <div class="hero-state">
        <div class="hero-state-tag">FLEET OPERATIONAL STATUS</div>
        <div class="hero-state-val" :class="`tone-${fleetTone}`">
          <span class="state-dot" />{{ overview.fleetState || '——' }}
        </div>
        <div class="hero-state-meta">
          {{ overview.shiftDate }} · {{ overview.shiftCode }} SHIFT ·
          <b>{{ overview.shiftRange }}</b>
        </div>
      </div>

      <div class="hero-tickers">
        <div class="ticker">
          <div class="ticker-lbl">机队可放行</div>
          <div class="ticker-val">
            {{ overview.fleetReady }}<small>/{{ overview.fleetTotal }}</small>
          </div>
          <div class="ticker-sub up">
            ▲ {{ fleetPct }}% · {{ deltaSign(overview.fleetReadyDelta) }} vs YDA
          </div>
        </div>
        <div class="ticker">
          <div class="ticker-lbl">活跃工卡</div>
          <div class="ticker-val">{{ overview.activeWorkcards }}</div>
          <div class="ticker-sub warn">
            <span class="dot dot-danger" />{{ overview.workcardsByPriority?.p1 }} P1
            ·
            <span class="dot dot-warning" />{{ overview.workcardsByPriority?.p2 }} P2
            ·
            <span class="dot dot-info" />{{ overview.workcardsByPriority?.p3 }} P3
          </div>
        </div>
        <div class="ticker">
          <div class="ticker-lbl">故障开放</div>
          <div class="ticker-val tone-danger">{{ overview.openFaults }}</div>
          <div class="ticker-sub down">
            ▲ {{ deltaSign(overview.openFaultsDelta) }} ·
            {{ overview.openFaultsCritical }} 紧急
          </div>
        </div>
        <div class="ticker">
          <div class="ticker-lbl">AR 协作 LIVE</div>
          <div class="ticker-val tone-info">{{ overview.arSessionsLive }}</div>
          <div class="ticker-sub">
            <span class="dot dot-info" />{{ overview.arTechniciansOnline }} 技师在线
          </div>
        </div>
      </div>

      <div class="hero-shift">
        <div class="ticker-lbl">当班进度</div>
        <div class="hero-shift-name">{{ overview.shiftCode }} · BLOCK B</div>
        <el-progress
          :percentage="shiftProgress"
          :show-text="false"
          :stroke-width="6"
          class="hero-shift-bar"
        />
        <div class="hero-shift-meta">
          <span>{{ overview.shiftRange?.split('-')[0] }}</span>
          <b>{{ clock }} · {{ shiftProgress }}%</b>
          <span>{{ overview.shiftRange?.split('-')[1] }}</span>
        </div>
      </div>

      <!-- 滚动告警 -->
      <div v-if="overview.alerts?.length" class="alert-ticker">
        <span class="alert-chip">ALERT</span>
        <div class="alert-track">
          <span v-for="(a, i) in [...overview.alerts, ...overview.alerts]" :key="i">
            <b>{{ a.id }}</b> {{ a.message }} · {{ a.time }}
          </span>
        </div>
      </div>
    </section>

    <!-- ═══════════ KPI 行 ═══════════ -->
    <section class="kpi-row">
      <div
        v-for="k in kpis"
        :key="k.key"
        class="kpi"
        :class="`tone-${k.tone}`"
      >
        <div class="kpi-head">
          <span class="kpi-lbl">{{ k.label }}</span>
          <span class="kpi-tag">{{ k.suffix }}</span>
        </div>
        <div class="kpi-val">
          {{ k.value }}<small v-if="k.unit">{{ k.unit }}</small>
        </div>
        <div class="kpi-foot">
          <span class="kpi-delta" :class="`d-${k.deltaTone}`">
            <template v-if="k.deltaTone === 'up'">▲ +{{ k.delta }}</template>
            <template v-else-if="k.deltaTone === 'down'">▼ +{{ k.delta }}</template>
            <template v-else>● 持平</template>
            <span class="muted">/24h</span>
          </span>
          <svg class="spark" viewBox="0 0 60 22" preserveAspectRatio="none">
            <polyline
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
              :points="sparkPoints(k.spark)"
            />
          </svg>
        </div>
      </div>
    </section>

    <!-- ═══════════ 主网格 ═══════════ -->
    <section class="grid-main">
      <!-- 进行中工卡 -->
      <el-card class="panel panel-wc" shadow="never">
        <template #header>
          <div class="panel-head">
            <div class="panel-title">
              <el-icon><Tickets /></el-icon>
              <span>进行中工卡</span>
              <small>LIVE · {{ workcards.length }}</small>
            </div>
            <el-radio-group v-model="wcFilter" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="P1">P1</el-radio-button>
              <el-radio-button label="P2">P2</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <el-table
          :data="filteredWorkcards"
          size="small"
          stripe
          class="wc-table"
          @row-click="(r) => goWorkcard(r.id)"
        >
          <el-table-column prop="cardNo" label="WC#" width="110">
            <template #default="{ row }">
              <span class="mono">{{ row.cardNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="优先级" width="80">
            <template #default="{ row }">
              <span class="prio" :class="`prio-${row.priority.toLowerCase()}`">
                {{ row.priority }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="tail" label="机号" width="92">
            <template #default="{ row }">
              <span class="mono strong">{{ row.tail }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="system" label="系统" min-width="120" />
          <el-table-column label="技师" width="120">
            <template #default="{ row }">
              <div class="tech">
                <span class="tech-av">{{ row.techInitial }}</span>
                {{ row.technician }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="170">
            <template #default="{ row }">
              <div class="wc-progress">
                <el-progress
                  :percentage="row.progress"
                  :show-text="false"
                  :stroke-width="6"
                  :status="progressStatus(row.progress)"
                />
                <span class="mono">{{ row.progress }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="eta" label="ETA" width="92">
            <template #default="{ row }">
              <span class="mono">{{ row.eta }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 机库占位 -->
      <el-card class="panel panel-hangar" shadow="never">
        <template #header>
          <div class="panel-head">
            <div class="panel-title">
              <el-icon><OfficeBuilding /></el-icon>
              <span>机库占位</span>
              <small>{{ bays.length }} BAYS</small>
            </div>
            <el-button text size="small" @click="$router.push('/mro/hangar')">
              详情 →
            </el-button>
          </div>
        </template>
        <div class="bay-grid">
          <div
            v-for="b in bays"
            :key="b.id"
            class="bay"
            :class="`bay-${b.status}`"
          >
            <div class="bay-id">{{ b.code }}</div>
            <div class="bay-tail">{{ b.tail || '——' }}</div>
            <div class="bay-status">
              <span class="state-dot" />{{ b.label }}
            </div>
            <svg class="bay-plane" viewBox="0 0 64 48">
              <path d="M2 26h26l8-14h6l-2 14h12l4-6h4l-2 8 2 8h-4l-4-6H40l2 14h-6l-8-14H2z"
                fill="currentColor"/>
            </svg>
          </div>
        </div>
      </el-card>

      <!-- AR 协作 -->
      <el-card class="panel panel-ar" shadow="never">
        <template #header>
          <div class="panel-head">
            <div class="panel-title">
              <el-icon><VideoCamera /></el-icon>
              <span>AR 协作 · LIVE</span>
              <small>{{ arSessions.length }} SESSIONS</small>
            </div>
            <el-button text size="small" @click="$router.push('/mro/ar/sessions')">
              全部 →
            </el-button>
          </div>
        </template>
        <div class="ar-list">
          <div v-for="s in arSessions" :key="s.id" class="ar-row">
            <div class="ar-pulse" :class="`tone-${s.tone}`">
              <el-icon><VideoCamera /></el-icon>
            </div>
            <div class="ar-mid">
              <div class="ar-title">{{ s.id }} · {{ s.topic }}</div>
              <div class="ar-meta">
                <span class="mono">{{ s.tail }} · {{ s.system }}</span>
                · 现场 <b>{{ s.onSite }}</b> ↔ <b>{{ s.remote }}</b>
              </div>
            </div>
            <div class="ar-right">
              <span class="ar-dur mono" :class="`tone-${s.tone}`">
                {{ formatDuration(s.durationSec) }}
              </span>
              <div class="ar-ppl">
                <span
                  v-for="(p, i) in s.participants"
                  :key="i"
                  class="ppl-av"
                  :class="`tone-${p.tone}`"
                >{{ p.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </section>

    <!-- ═══════════ 底部三栏 ═══════════ -->
    <section class="grid-bottom">
      <!-- 资质合规仪表 -->
      <el-card class="panel" shadow="never">
        <template #header>
          <div class="panel-head">
            <div class="panel-title">
              <el-icon><Reading /></el-icon>
              <span>资质合规率</span>
              <small>30D</small>
            </div>
          </div>
        </template>
        <div class="gauge-wrap">
          <div class="gauge">
            <svg viewBox="0 0 100 100">
              <circle class="gauge-track" cx="50" cy="50" r="42" />
              <circle
                class="gauge-arc"
                cx="50"
                cy="50"
                r="42"
                :stroke-dasharray="263.89"
                :stroke-dashoffset="gaugeDash"
              />
            </svg>
            <div class="gauge-center">
              <b>{{ overview.trainingCompliance }}<small>%</small></b>
              <span>COMPLIANCE</span>
            </div>
          </div>
          <div class="gauge-meta">
            <div class="gm-row">
              <span>已合规</span>
              <b>{{ overview.trainingCompliantCount }} 人</b>
            </div>
            <div class="gm-row">
              <span>30天到期</span>
              <b class="tone-warning">{{ overview.trainingExpiring30d }} 人</b>
            </div>
            <div class="gm-row">
              <span>已逾期</span>
              <b class="tone-danger">{{ overview.trainingOverdue }} 人</b>
            </div>
            <div class="gm-row">
              <span>VR课时本月</span>
              <b>{{ overview.vrHoursThisMonth }} h</b>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 故障 ATA 分布 -->
      <el-card class="panel" shadow="never">
        <template #header>
          <div class="panel-head">
            <div class="panel-title">
              <el-icon><TrendCharts /></el-icon>
              <span>故障分布</span>
              <small>BY ATA · 7D</small>
            </div>
          </div>
        </template>
        <div class="cats">
          <div v-for="c in faults" :key="c.ata" class="cat-row">
            <div class="cat-name">
              <span class="mono cat-ata">{{ c.ata }}</span>
              {{ c.name }}
            </div>
            <div class="cat-bar">
              <i :class="`tone-${c.tone}`" :style="{ width: catWidth(c.count) }" />
            </div>
            <span class="cat-num mono">{{ c.count }}</span>
          </div>
        </div>
      </el-card>

      <!-- 系统事件流 -->
      <el-card class="panel panel-log" shadow="never">
        <template #header>
          <div class="panel-head">
            <div class="panel-title">
              <el-icon><DataLine /></el-icon>
              <span>系统事件流</span>
              <small>LIVE</small>
            </div>
            <span class="live-pulse"><span class="pulse-dot" /></span>
          </div>
        </template>
        <div class="log">
          <div
            v-for="(e, i) in events"
            :key="i"
            class="log-row"
          >
            <span class="log-time mono">{{ e.time }}</span>
            <span class="log-lvl mono" :class="`lvl-${e.level.toLowerCase()}`">
              {{ e.level }}
            </span>
            <span class="log-msg">{{ e.message }}</span>
          </div>
        </div>
      </el-card>
    </section>

    <!-- ═══════════ 系统信息 ═══════════ -->
    <el-card class="panel sysinfo" shadow="never">
      <el-descriptions :column="5" border size="small">
        <el-descriptions-item label="系统名称">
          {{ systemInfo.systemName }}
        </el-descriptions-item>
        <el-descriptions-item label="系统版本">
          <span class="mono">{{ systemInfo.systemVersion }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="构建时间">
          <span class="mono">{{ systemInfo.buildTime }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="运行时长">
          <span class="mono">{{ systemInfo.uptime }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="登录用户">
          {{ userName }} · {{ systemInfo.region }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth'
import {
  getOverview,
  getKpis,
  getActiveWorkcards,
  getHangarBays,
  getArLive,
  getFaultsByAta,
  getEvents,
  getSystemInfo,
} from '@/api/dashboard'

const router = useRouter()
const authStore = useAuthStore()

const overview = ref({})
const kpis = ref([])
const workcards = ref([])
const bays = ref([])
const arSessions = ref([])
const faults = ref([])
const events = ref([])
const systemInfo = ref({})

const wcFilter = ref('all')
const filteredWorkcards = computed(() =>
  wcFilter.value === 'all'
    ? workcards.value
    : workcards.value.filter(w => w.priority === wcFilter.value),
)

const userName = computed(() => authStore.user?.realName || '用户')

const fleetPct = computed(() => {
  if (!overview.value.fleetTotal) return 0
  return ((overview.value.fleetReady / overview.value.fleetTotal) * 100).toFixed(1)
})
const fleetTone = computed(() => {
  const s = overview.value.fleetState
  if (s === 'NOMINAL') return 'success'
  if (s === 'CAUTION') return 'warning'
  if (s === 'CRITICAL') return 'danger'
  return 'info'
})

// 仪表环：周长 263.89，已合规比例反向偏移
const gaugeDash = computed(() => {
  const pct = (overview.value.trainingCompliance ?? 0) / 100
  return (263.89 * (1 - pct)).toFixed(2)
})

// 故障条最大值参照取最大 count
const catMax = computed(() => Math.max(1, ...faults.value.map(f => f.count)))
const catWidth = (n) => `${Math.max(6, (n / catMax.value) * 100)}%`

const sparkPoints = (arr = []) => {
  if (!arr.length) return ''
  const w = 60, h = 22
  const max = Math.max(...arr), min = Math.min(...arr)
  const range = max - min || 1
  return arr
    .map((v, i) => {
      const x = (i / (arr.length - 1)) * w
      const y = h - ((v - min) / range) * h
      return `${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')
}

const deltaSign = (n) => (n > 0 ? `+${n}` : n < 0 ? `${n}` : '0')

const formatDuration = (sec = 0) => {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const progressStatus = (p) => {
  if (p >= 90) return 'success'
  if (p < 35) return 'exception'
  return ''
}

// 当班进度（基于 shiftRange 与本地时间）
const clock = ref('')
const shiftProgress = ref(0)
let timer = null

const tick = () => {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  clock.value = `${pad(d.getHours())}:${pad(d.getMinutes())}`
  const range = (overview.value.shiftRange || '06:00-18:00').split('-')
  const [sh, sm] = range[0].split(':').map(Number)
  const [eh, em] = range[1].split(':').map(Number)
  const total = (eh * 60 + em) - (sh * 60 + sm)
  const passed = (d.getHours() * 60 + d.getMinutes()) - (sh * 60 + sm)
  shiftProgress.value = Math.max(0, Math.min(100, Math.round((passed / total) * 100)))
}

const goWorkcard = (id) => router.push(`/mro/workcard?id=${id}`)

const loadAll = async () => {
  try {
    const [o, k, w, b, a, f, e, s] = await Promise.all([
      getOverview(), getKpis(), getActiveWorkcards(), getHangarBays(),
      getArLive(), getFaultsByAta(), getEvents(), getSystemInfo(),
    ])
    overview.value   = o.data || {}
    kpis.value       = k.data || []
    workcards.value  = w.data || []
    bays.value       = b.data || []
    arSessions.value = a.data || []
    faults.value     = f.data || []
    events.value     = e.data || []
    systemInfo.value = s.data || {}
    tick()
  } catch (err) {
    console.error('[dashboard] load failed', err)
  }
}

onMounted(() => {
  loadAll()
  timer = setInterval(tick, 30 * 1000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
/* ═══════════ tokens（绑定主题变量 + 本地状态色） ═══════════ */
.ops {
  --c-primary: var(--el-color-primary, #0066cc);
  --c-success: var(--el-color-success, #16a34a);
  --c-warning: var(--el-color-warning, #f59e0b);
  --c-danger:  var(--el-color-danger,  #ef4444);
  --c-info:    var(--el-color-info,    #5fd9ff);
  --ink-1: #1f2937;
  --ink-2: #4b5563;
  --ink-3: #9ca3af;
  --ink-4: #d1d5db;
  --line:  #e5e7eb;
  --bg-soft: #f8fafc;
  --bg-panel: #ffffff;

  display: flex;
  flex-direction: column;
  gap: 14px;
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
}
.mono {
  font-family: "JetBrains Mono", "SFMono-Regular", Menlo, Consolas, monospace;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.2px;
}
.mono.strong { font-weight: 600; color: var(--ink-1); }

/* ═══════════ HERO STRIP ═══════════ */
.ops-hero {
  display: grid;
  grid-template-columns: 240px 1fr 220px;
  background: var(--bg-panel);
  border: 1px solid var(--line);
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}
.ops-hero::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(90deg, var(--el-color-primary-light-7, #cfe0ff) 1px, transparent 1px),
    linear-gradient(0deg, var(--el-color-primary-light-7, #cfe0ff) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: radial-gradient(ellipse 360px 180px at 12% 50%, #000 0%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse 360px 180px at 12% 50%, #000 0%, transparent 70%);
  opacity: 0.55;
}
.hero-state, .hero-tickers, .hero-shift { padding: 16px 18px; position: relative; }
.hero-state { border-right: 1px solid var(--line); display: flex; flex-direction: column; gap: 4px; justify-content: center; }
.hero-state-tag {
  font-family: "JetBrains Mono", monospace;
  font-size: 10px; letter-spacing: 1.6px; color: var(--ink-3); text-transform: uppercase;
}
.hero-state-val {
  font-family: "JetBrains Mono", monospace; font-weight: 700;
  font-size: 28px; letter-spacing: 1px; line-height: 1;
  display: inline-flex; align-items: center; gap: 10px;
}
.hero-state-val .state-dot { width: 10px; height: 10px; border-radius: 50%; background: currentColor; box-shadow: 0 0 10px currentColor; }
.hero-state-meta {
  font-family: "JetBrains Mono", monospace; font-size: 12px; color: var(--ink-2);
}
.hero-state-meta b { color: var(--c-primary); font-weight: 600; }

.hero-tickers { display: grid; grid-template-columns: repeat(4, 1fr); align-items: center; gap: 0; padding: 16px 0; }
.ticker { padding: 0 18px; border-left: 1px solid var(--line); display: flex; flex-direction: column; gap: 4px; }
.ticker:first-child { border-left: 0; }
.ticker-lbl {
  font-family: "JetBrains Mono", monospace;
  font-size: 10px; letter-spacing: 1.4px; color: var(--ink-3); text-transform: uppercase;
}
.ticker-val {
  font-family: "JetBrains Mono", monospace; font-weight: 700;
  font-size: 22px; color: var(--ink-1); letter-spacing: 0.3px; line-height: 1;
  display: flex; align-items: baseline; gap: 2px;
}
.ticker-val small { font-weight: 500; font-size: 12px; color: var(--ink-3); }
.ticker-sub { font-family: "JetBrains Mono", monospace; font-size: 11px; color: var(--ink-2); }
.ticker-sub.up { color: var(--c-success); }
.ticker-sub.down { color: var(--c-danger); }
.ticker-sub.warn { color: var(--ink-2); }
.dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; vertical-align: 1px; margin-right: 2px; }
.dot-danger { background: var(--c-danger); box-shadow: 0 0 6px var(--c-danger); }
.dot-warning { background: var(--c-warning); box-shadow: 0 0 6px var(--c-warning); }
.dot-info { background: var(--c-info); box-shadow: 0 0 6px var(--c-info); }

.hero-shift { border-left: 1px solid var(--line); display: flex; flex-direction: column; justify-content: center; gap: 6px; }
.hero-shift-name { font-weight: 700; font-size: 16px; color: var(--ink-1); letter-spacing: 0.4px; }
.hero-shift-bar :deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, var(--c-primary), var(--el-color-primary-light-3, #6ea7e8));
}
.hero-shift-meta { font-family: "JetBrains Mono", monospace; font-size: 11px; color: var(--ink-2); display: flex; justify-content: space-between; }
.hero-shift-meta b { color: var(--ink-1); }

.alert-ticker {
  grid-column: 1 / -1;
  border-top: 1px solid var(--line);
  padding: 8px 18px;
  display: flex; align-items: center; gap: 14px;
  background: color-mix(in srgb, var(--c-danger) 5%, transparent);
  font-family: "JetBrains Mono", monospace; font-size: 11px;
  overflow: hidden;
}
.alert-chip {
  flex: 0 0 auto; padding: 2px 8px; border-radius: 3px;
  background: color-mix(in srgb, var(--c-danger) 18%, transparent);
  color: var(--c-danger);
  font-size: 10px; letter-spacing: 1.4px; font-weight: 700;
}
.alert-track {
  display: flex; gap: 32px;
  white-space: nowrap; color: var(--ink-2);
  animation: scroll 38s linear infinite;
}
.alert-track b { color: var(--c-warning); margin-right: 6px; }
@keyframes scroll { from { transform: translateX(0); } to { transform: translateX(-50%); } }

/* ═══════════ KPI 行 ═══════════ */
.kpi-row { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.kpi {
  position: relative; overflow: hidden;
  background: var(--bg-panel); border: 1px solid var(--line); border-radius: 8px;
  padding: 12px 14px;
  transition: border-color 0.18s, transform 0.18s;
  cursor: default;
}
.kpi:hover { border-color: var(--c-primary); transform: translateY(-1px); }
.kpi::before {
  content: ""; position: absolute; left: 0; top: 0; bottom: 0; width: 3px;
  background: var(--ink-4);
}
.kpi.tone-primary::before { background: var(--c-primary); }
.kpi.tone-success::before { background: var(--c-success); }
.kpi.tone-warning::before { background: var(--c-warning); }
.kpi.tone-danger::before  { background: var(--c-danger); }
.kpi.tone-info::before    { background: var(--c-info); }

.kpi-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.kpi-lbl {
  font-family: "JetBrains Mono", monospace; font-size: 10px; letter-spacing: 1.4px;
  color: var(--ink-3); text-transform: uppercase;
}
.kpi-tag {
  font-family: "JetBrains Mono", monospace; font-size: 10px; color: var(--ink-3);
  padding: 1px 6px; border-radius: 3px; background: var(--bg-soft);
}
.kpi-val {
  font-family: "JetBrains Mono", monospace; font-weight: 700;
  font-size: 28px; color: var(--ink-1); line-height: 1; letter-spacing: 0.4px;
  display: flex; align-items: baseline; gap: 2px;
}
.kpi-val small { font-size: 13px; font-weight: 500; color: var(--ink-3); }
.kpi-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.kpi-delta { font-family: "JetBrains Mono", monospace; font-size: 11px; color: var(--ink-2); display: inline-flex; align-items: center; gap: 4px; }
.kpi-delta .muted { color: var(--ink-3); }
.kpi-delta.d-up { color: var(--c-success); }
.kpi-delta.d-down { color: var(--c-danger); }
.kpi-delta.d-flat { color: var(--ink-2); }
.spark { width: 60px; height: 22px; }
.kpi.tone-primary .spark { color: var(--c-primary); }
.kpi.tone-success .spark { color: var(--c-success); }
.kpi.tone-warning .spark { color: var(--c-warning); }
.kpi.tone-danger  .spark { color: var(--c-danger); }
.kpi.tone-info    .spark { color: var(--c-info); }

/* ═══════════ 主网格 ═══════════ */
.grid-main { display: grid; grid-template-columns: minmax(0, 1.5fr) minmax(0, 1fr) minmax(0, 1fr); gap: 12px; }
.grid-bottom { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1.4fr) minmax(0, 1fr); gap: 12px; }

.panel { border-radius: 8px; border: 1px solid var(--line); }
.panel :deep(.el-card__header) { padding: 12px 16px; border-bottom: 1px solid var(--line); }
.panel :deep(.el-card__body) { padding: 0; }
.panel-head { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.panel-title { display: flex; align-items: center; gap: 8px; color: var(--ink-1); font-weight: 600; }
.panel-title .el-icon { color: var(--c-primary); }
.panel-title small {
  font-family: "JetBrains Mono", monospace; font-size: 10px; color: var(--ink-3);
  letter-spacing: 1.4px; font-weight: 500; text-transform: uppercase; margin-left: 4px;
}
.live-pulse { display: inline-flex; align-items: center; }
.pulse-dot {
  width: 8px; height: 8px; border-radius: 50%; background: var(--c-success);
  animation: pulse 1.8s ease-out infinite;
}
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 color-mix(in srgb, var(--c-success) 60%, transparent); }
  80% { box-shadow: 0 0 0 8px color-mix(in srgb, var(--c-success) 0%, transparent); }
  100% { box-shadow: 0 0 0 0 color-mix(in srgb, var(--c-success) 0%, transparent); }
}

/* 工卡表 */
.wc-table { font-size: 12px; }
.wc-table :deep(thead th) {
  font-family: "JetBrains Mono", monospace; font-size: 10px;
  color: var(--ink-3); letter-spacing: 1.4px; text-transform: uppercase;
  background: var(--bg-soft) !important;
}
.wc-table :deep(.el-table__row) { cursor: pointer; }
.prio {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 2px 8px; border-radius: 3px;
  font-family: "JetBrains Mono", monospace; font-size: 10px; font-weight: 700; letter-spacing: 1px;
}
.prio::before { content: ""; width: 5px; height: 5px; border-radius: 50%; background: currentColor; box-shadow: 0 0 6px currentColor; }
.prio-p1 { color: var(--c-danger);  background: color-mix(in srgb, var(--c-danger)  14%, transparent); }
.prio-p2 { color: var(--c-warning); background: color-mix(in srgb, var(--c-warning) 16%, transparent); }
.prio-p3 { color: var(--c-info);    background: color-mix(in srgb, var(--c-info)    18%, transparent); }
.prio-p4 { color: var(--ink-2);     background: var(--bg-soft); }
.tech { display: flex; align-items: center; gap: 6px; }
.tech-av {
  width: 22px; height: 22px; border-radius: 50%;
  background: var(--el-color-primary-light-8, #e7eefb); color: var(--c-primary);
  display: inline-grid; place-items: center;
  font-family: "JetBrains Mono", monospace; font-size: 9px; font-weight: 700;
}
.wc-progress { display: flex; align-items: center; gap: 8px; }
.wc-progress .el-progress { flex: 1; }
.wc-progress .mono { font-size: 11px; color: var(--ink-2); min-width: 32px; text-align: right; }

/* 机库 */
.bay-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; padding: 14px; }
.bay {
  position: relative; overflow: hidden; cursor: pointer;
  aspect-ratio: 1.45 / 1; border: 1px solid var(--line); border-radius: 7px;
  background: var(--bg-soft);
  padding: 10px 12px; display: flex; flex-direction: column; justify-content: space-between; gap: 4px;
  transition: border-color 0.15s, transform 0.15s;
}
.bay:hover { transform: translateY(-1px); }
.bay-id { font-family: "JetBrains Mono", monospace; font-size: 10px; letter-spacing: 1.4px; color: var(--ink-3); }
.bay-tail { font-family: "JetBrains Mono", monospace; font-weight: 700; font-size: 15px; letter-spacing: 0.4px; color: var(--ink-1); }
.bay-status {
  font-family: "JetBrains Mono", monospace; font-size: 10px; letter-spacing: 0.6px;
  color: var(--ink-2); display: inline-flex; align-items: center; gap: 6px;
}
.bay-status .state-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; box-shadow: 0 0 6px currentColor; }
.bay-plane { position: absolute; right: -6px; bottom: -4px; width: 64px; height: 48px; color: var(--ink-4); opacity: 0.5; }
.bay-maintenance { border-color: color-mix(in srgb, var(--c-warning) 45%, var(--line)); background: color-mix(in srgb, var(--c-warning) 6%, var(--bg-panel)); }
.bay-maintenance .bay-tail { color: var(--c-warning); }
.bay-maintenance .bay-status { color: var(--c-warning); }
.bay-fault { border-color: color-mix(in srgb, var(--c-danger) 50%, var(--line)); background: color-mix(in srgb, var(--c-danger) 7%, var(--bg-panel)); }
.bay-fault .bay-tail { color: var(--c-danger); }
.bay-fault .bay-status { color: var(--c-danger); }
.bay-ready { border-color: color-mix(in srgb, var(--c-success) 45%, var(--line)); background: color-mix(in srgb, var(--c-success) 6%, var(--bg-panel)); }
.bay-ready .bay-tail { color: var(--c-success); }
.bay-ready .bay-status { color: var(--c-success); }
.bay-empty { opacity: 0.65; border-style: dashed; }
.bay-empty .bay-status { color: var(--ink-3); }
.bay-empty .bay-tail { color: var(--ink-3); }

/* AR 列表 */
.ar-list { display: flex; flex-direction: column; }
.ar-row {
  padding: 12px 16px; border-bottom: 1px solid var(--line);
  display: grid; grid-template-columns: auto 1fr auto; gap: 12px; align-items: center;
}
.ar-row:last-child { border-bottom: 0; }
.ar-pulse {
  width: 36px; height: 36px; border-radius: 8px;
  display: grid; place-items: center; position: relative;
  border: 1px solid; color: var(--c-primary);
}
.ar-pulse.tone-primary { background: color-mix(in srgb, var(--c-primary) 8%, transparent); border-color: color-mix(in srgb, var(--c-primary) 40%, transparent); color: var(--c-primary); }
.ar-pulse.tone-success { background: color-mix(in srgb, var(--c-success) 8%, transparent); border-color: color-mix(in srgb, var(--c-success) 40%, transparent); color: var(--c-success); }
.ar-pulse.tone-warning { background: color-mix(in srgb, var(--c-warning) 10%, transparent); border-color: color-mix(in srgb, var(--c-warning) 40%, transparent); color: var(--c-warning); }
.ar-pulse::after {
  content: ""; position: absolute; inset: -3px; border-radius: 10px; border: 1px solid currentColor;
  opacity: 0.5; animation: ringpulse 2s ease-out infinite;
}
@keyframes ringpulse { 0% { transform: scale(0.9); opacity: 0.7; } 100% { transform: scale(1.2); opacity: 0; } }
.ar-mid { min-width: 0; }
.ar-title { font-weight: 600; font-size: 13px; color: var(--ink-1); letter-spacing: 0.2px; }
.ar-meta { font-family: "JetBrains Mono", monospace; font-size: 11px; color: var(--ink-2); margin-top: 2px; }
.ar-meta b { color: var(--ink-1); font-weight: 600; }
.ar-right { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.ar-dur { font-size: 13px; font-weight: 600; }
.ar-dur.tone-primary { color: var(--c-primary); }
.ar-dur.tone-success { color: var(--c-success); }
.ar-dur.tone-warning { color: var(--c-warning); }
.ar-ppl { display: flex; }
.ppl-av {
  width: 22px; height: 22px; border-radius: 50%;
  display: grid; place-items: center;
  font-family: "JetBrains Mono", monospace; font-size: 9px; font-weight: 700;
  margin-left: -6px; border: 2px solid var(--bg-panel);
}
.ppl-av:first-child { margin-left: 0; }
.ppl-av.tone-primary { background: color-mix(in srgb, var(--c-primary) 18%, white); color: var(--c-primary); }
.ppl-av.tone-success { background: color-mix(in srgb, var(--c-success) 20%, white); color: var(--c-success); }
.ppl-av.tone-warning { background: color-mix(in srgb, var(--c-warning) 22%, white); color: #8a4500; }
.ppl-av.tone-info    { background: color-mix(in srgb, var(--c-info)    22%, white); color: #0e6a82; }
.ppl-av.tone-danger  { background: color-mix(in srgb, var(--c-danger)  20%, white); color: var(--c-danger); }

/* 仪表 */
.gauge-wrap { display: flex; gap: 18px; padding: 16px; align-items: center; }
.gauge { width: 130px; height: 130px; flex: 0 0 130px; position: relative; }
.gauge svg { width: 100%; height: 100%; transform: rotate(-90deg); }
.gauge-track { stroke: var(--bg-soft); fill: none; stroke-width: 9; }
.gauge-arc {
  stroke: var(--c-primary); fill: none; stroke-width: 9; stroke-linecap: round;
  filter: drop-shadow(0 0 6px color-mix(in srgb, var(--c-primary) 40%, transparent));
  transition: stroke-dashoffset 0.6s ease-out;
}
.gauge-center {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2px;
}
.gauge-center b {
  font-family: "JetBrains Mono", monospace; font-weight: 700;
  font-size: 32px; color: var(--ink-1); line-height: 1;
}
.gauge-center b small { font-size: 16px; color: var(--ink-3); margin-left: 2px; font-weight: 500; }
.gauge-center span {
  font-family: "JetBrains Mono", monospace; font-size: 9px;
  letter-spacing: 1.4px; color: var(--ink-3); margin-top: 4px; text-transform: uppercase;
}
.gauge-meta { flex: 1; display: flex; flex-direction: column; gap: 8px; font-size: 12px; font-family: "JetBrains Mono", monospace; }
.gm-row { display: flex; justify-content: space-between; color: var(--ink-2); }
.gm-row b { color: var(--ink-1); font-weight: 600; }
.gm-row b.tone-warning { color: var(--c-warning); }
.gm-row b.tone-danger  { color: var(--c-danger); }

/* 故障分布条 */
.cats { padding: 12px 16px; display: flex; flex-direction: column; gap: 9px; }
.cat-row { display: grid; grid-template-columns: 96px 1fr 36px; gap: 10px; align-items: center; font-size: 12px; }
.cat-name { color: var(--ink-1); display: inline-flex; align-items: center; gap: 6px; }
.cat-ata { font-size: 10px; color: var(--ink-3); padding: 1px 5px; background: var(--bg-soft); border-radius: 3px; }
.cat-bar { height: 8px; background: var(--bg-soft); border-radius: 2px; overflow: hidden; }
.cat-bar i { display: block; height: 100%; }
.cat-bar i.tone-primary { background: linear-gradient(90deg, var(--c-primary), var(--el-color-primary-light-3, #6ea7e8)); }
.cat-bar i.tone-success { background: linear-gradient(90deg, var(--c-success), #5fffb6); }
.cat-bar i.tone-warning { background: linear-gradient(90deg, var(--c-warning), #ffb547); }
.cat-bar i.tone-danger  { background: linear-gradient(90deg, var(--c-danger), #ff8a96); }
.cat-bar i.tone-info    { background: linear-gradient(90deg, var(--c-info), var(--el-color-primary-light-2, #5b8def)); }
.cat-num { color: var(--ink-1); text-align: right; font-weight: 600; }

/* 事件流 */
.log {
  font-family: "JetBrains Mono", monospace; font-size: 11.5px; line-height: 1.7;
  max-height: 260px; overflow-y: auto;
}
.log-row {
  padding: 5px 16px; border-bottom: 1px solid var(--line);
  display: grid; grid-template-columns: 64px 56px 1fr; gap: 10px; align-items: baseline;
}
.log-row:last-child { border-bottom: 0; }
.log-time { color: var(--ink-3); font-size: 10.5px; }
.log-lvl { font-size: 10px; letter-spacing: 1.2px; font-weight: 700; }
.log-lvl.lvl-info { color: var(--c-info); }
.log-lvl.lvl-warn { color: var(--c-warning); }
.log-lvl.lvl-err  { color: var(--c-danger); }
.log-lvl.lvl-ok   { color: var(--c-success); }
.log-msg { color: var(--ink-2); }

/* 系统信息 */
.sysinfo :deep(.el-card__body) { padding: 16px; }
.sysinfo :deep(.el-descriptions__label) {
  font-family: "JetBrains Mono", monospace; font-size: 11px; letter-spacing: 0.5px;
  color: var(--ink-3); width: 96px;
}

/* 工具样式 */
.tone-success { color: var(--c-success); }
.tone-warning { color: var(--c-warning); }
.tone-danger  { color: var(--c-danger); }
.tone-info    { color: var(--c-info); }
.tone-primary { color: var(--c-primary); }

/* 响应式 */
@media (max-width: 1280px) {
  .ops-hero { grid-template-columns: 220px 1fr; }
  .hero-shift { grid-column: 2 / 3; border-left: 0; border-top: 1px solid var(--line); }
  .kpi-row { grid-template-columns: repeat(3, 1fr); }
  .grid-main { grid-template-columns: 1fr 1fr; }
  .grid-main > :first-child { grid-column: 1 / -1; }
  .grid-bottom { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .ops-hero { grid-template-columns: 1fr; }
  .hero-state, .hero-shift { border-right: 0; border-left: 0; }
  .hero-tickers { grid-template-columns: repeat(2, 1fr); border-top: 1px solid var(--line); border-bottom: 1px solid var(--line); }
  .kpi-row { grid-template-columns: repeat(2, 1fr); }
  .grid-main, .grid-bottom { grid-template-columns: 1fr; }
  .bay-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
