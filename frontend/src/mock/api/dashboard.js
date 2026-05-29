/**
 * Dashboard mock — MRO ops console aggregated data.
 * Refs: SYS-006 (后续如有正式 spec，同步对齐契约)
 */

const overview = {
  shiftDate: '2026-05-25',
  shiftCode: 'DAY',
  shiftRange: '06:00-18:00',
  fleetState: 'NOMINAL',
  fleetReady: 24,
  fleetTotal: 26,
  fleetReadyDelta: 1,
  activeWorkcards: 47,
  workcardsByPriority: { p1: 12, p2: 18, p3: 17 },
  openFaults: 8,
  openFaultsCritical: 1,
  openFaultsDelta: 2,
  arSessionsLive: 3,
  arTechniciansOnline: 7,
  toolsCheckedOut: 156,
  toolsOverdue: 1,
  trainingCompliance: 94,
  trainingCompliantCount: 218,
  trainingTotal: 232,
  trainingExpiring30d: 10,
  trainingOverdue: 4,
  vrHoursThisMonth: 1287,
  alerts: [
    { id: 'FLT-2287', severity: 'critical', message: '机号 B-6042 — 液压系统 P3 压力告警', time: '09:42' },
    { id: 'WC-14093', severity: 'warning', message: '工具借出超时 · 扭矩扳手 #TK-221 · 4h12m', time: '14:21' },
  ],
}

const kpis = [
  { key: 'workcards', label: '活跃工卡', value: 47, suffix: '12 P1', tone: 'primary',
    delta: 6, deltaTone: 'up', spark: [16, 14, 17, 11, 12, 8, 10, 5, 7] },
  { key: 'faults', label: '故障开放', value: 8, suffix: '1 紧急', tone: 'danger',
    delta: 2, deltaTone: 'down', spark: [18, 17, 16, 15, 12, 11, 8, 6, 4] },
  { key: 'fleet', label: '机队可用', value: 92.3, unit: '%', suffix: '24/26', tone: 'success',
    delta: 3.8, deltaTone: 'up', spark: [12, 14, 11, 10, 8, 9, 6, 5, 5] },
  { key: 'ar', label: 'AR · 协作', value: 3, suffix: '7 技师', tone: 'info',
    delta: 0, deltaTone: 'flat', spark: [15, 11, 13, 9, 11, 7, 9, 6, 8] },
  { key: 'training', label: '培训合规', value: 94, unit: '%', suffix: '218/232', tone: 'primary',
    delta: 1.2, deltaTone: 'up', spark: [14, 12, 13, 10, 11, 8, 9, 7, 6] },
  { key: 'tools', label: '工具借出', value: 156, suffix: '1 超时', tone: 'warning',
    delta: 12, deltaTone: 'up', spark: [12, 15, 11, 13, 9, 11, 8, 10, 7] },
]

const activeWorkcards = [
  { id: 14093, cardNo: 'WC-14093', priority: 'P1', tail: 'B-6042', system: 'ATA-29 液压',
    technician: '李志强', techInitial: 'LZ', progress: 32, eta: '+2h 40m' },
  { id: 14087, cardNo: 'WC-14087', priority: 'P1', tail: 'B-1907', system: 'ATA-72 发动机',
    technician: '王晓东', techInitial: 'WX', progress: 71, eta: '+45m' },
  { id: 14082, cardNo: 'WC-14082', priority: 'P2', tail: 'B-3318', system: 'ATA-32 起落架',
    technician: '陈宇', techInitial: 'CY', progress: 88, eta: '+12m' },
  { id: 14079, cardNo: 'WC-14079', priority: 'P2', tail: 'B-6042', system: 'ATA-27 飞控',
    technician: '赵航', techInitial: 'ZH', progress: 54, eta: '+1h 20m' },
  { id: 14066, cardNo: 'WC-14066', priority: 'P3', tail: 'B-2280', system: 'ATA-25 客舱',
    technician: '孙明', techInitial: 'SM', progress: 96, eta: '+5m' },
  { id: 14059, cardNo: 'WC-14059', priority: 'P3', tail: 'B-7105', system: 'ATA-24 电气',
    technician: '林佳', techInitial: 'LJ', progress: 42, eta: '+2h 10m' },
  { id: 14051, cardNo: 'WC-14051', priority: 'P4', tail: 'B-1907', system: 'ATA-53 结构',
    technician: '胡强', techInitial: 'HQ', progress: 18, eta: '+5h' },
]

const hangarBays = [
  { id: 1, code: 'BAY-01', tail: 'B-6042', status: 'maintenance', label: '维护中 · A-CHECK' },
  { id: 2, code: 'BAY-02', tail: 'B-1907', status: 'fault',       label: '故障 · ATA-72'      },
  { id: 3, code: 'BAY-03', tail: 'B-3318', status: 'ready',       label: '就绪 · 待签放'       },
  { id: 4, code: 'BAY-04', tail: 'B-7105', status: 'maintenance', label: '维护中 · B-CHECK' },
  { id: 5, code: 'BAY-05', tail: 'B-2280', status: 'ready',       label: '就绪 · 14:35 出库' },
  { id: 6, code: 'BAY-06', tail: null,     status: 'empty',       label: '空闲 · 16:00 入位' },
]

const arSessions = [
  { id: 'AR-S/2287', topic: '液压故障定位', tail: 'B-6042', system: 'ATA-29',
    onSite: '李志强', remote: '王工', tone: 'primary', durationSec: 22 * 60 + 41,
    participants: [{ name: '李', tone: 'primary' }, { name: '王', tone: 'info' }] },
  { id: 'AR-S/2289', topic: '工卡复核', tail: 'B-1907', system: 'ATA-72',
    onSite: '王晓东', remote: '陈工(质检)', tone: 'warning', durationSec: 14 * 60 + 8,
    participants: [{ name: '王', tone: 'warning' }, { name: '陈', tone: 'success' }] },
  { id: 'AR-S/2290', topic: '新员工带教', tail: '培训机', system: '起落架',
    onSite: '赵航', remote: '学员 ×3', tone: 'success', durationSec: 8 * 60 + 53,
    participants: [
      { name: '赵', tone: 'success' }, { name: 'L', tone: 'info' },
      { name: 'M', tone: 'primary' }, { name: '+1', tone: 'warning' },
    ] },
]

const faultsByAta = [
  { ata: '29', name: '液压',   count: 14, tone: 'warning' },
  { ata: '72', name: '发动机', count: 12, tone: 'danger'  },
  { ata: '27', name: '飞控',   count: 10, tone: 'info'    },
  { ata: '32', name: '起落架', count: 8,  tone: 'primary' },
  { ata: '24', name: '电气',   count: 6,  tone: 'success' },
  { ata: '25', name: '客舱',   count: 4,  tone: 'warning' },
  { ata: '53', name: '结构',   count: 3,  tone: 'info'    },
]

const events = [
  { time: '14:21:08', level: 'WARN', message: '工具 TK-221 借出超时 4h12m · 持有人 王晓东' },
  { time: '14:20:42', level: 'INFO', message: 'AR-S/2290 已加入学员 李明' },
  { time: '14:19:55', level: 'OK',   message: '工卡 WC-14066 步骤 12/13 完成' },
  { time: '14:18:31', level: 'INFO', message: 'B-2280 出库申请已提交 · 14:35' },
  { time: '14:17:09', level: 'ERR',  message: 'FLT-2287 · B-6042 P3 液压压力异常 3,180 psi' },
  { time: '14:15:24', level: 'OK',   message: '手册 AMM-29-32-00 已同步至终端 ×12' },
  { time: '14:14:02', level: 'INFO', message: 'VR课时记录 +2h · 张磊 · 起落架紧急释放' },
  { time: '14:12:48', level: 'WARN', message: 'B-1907 ATA-72 故障升级 P1 → 排故助手已介入' },
  { time: '14:10:33', level: 'OK',   message: '签放 WC-14041 · B-3318 完成 A-CHECK' },
]

const systemInfo = {
  systemName: '智慧机务',
  systemVersion: '2.4.108',
  buildTime: '2026-05-24 03:12',
  uptime: '17d 04h',
  region: 'CST · UTC+08',
}

export default [
  { url: '/api/dashboard/overview',           method: 'get', response: () => ({ code: 200, data: overview }) },
  { url: '/api/dashboard/kpis',               method: 'get', response: () => ({ code: 200, data: kpis }) },
  { url: '/api/dashboard/workcards/active',   method: 'get', response: () => ({ code: 200, data: activeWorkcards }) },
  { url: '/api/dashboard/hangar/bays',        method: 'get', response: () => ({ code: 200, data: hangarBays }) },
  { url: '/api/dashboard/ar/live',            method: 'get', response: () => ({ code: 200, data: arSessions }) },
  { url: '/api/dashboard/faults/by-ata',      method: 'get', response: () => ({ code: 200, data: faultsByAta }) },
  { url: '/api/dashboard/events',             method: 'get', response: () => ({ code: 200, data: events }) },
  { url: '/api/dashboard/system-info',        method: 'get', response: () => ({ code: 200, data: systemInfo }) },
]
