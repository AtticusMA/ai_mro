export default [
  {
    url: '/api/dashboard/operation',
    method: 'get',
    response: () => ({
      "code": 200,
      "data": {
        "summary": {
          "total_aircraft": 8,
          "in_maintenance": 5,
          "total_workcards": 42,
          "completed_today": 7,
          "pending_quality": 4,
          "overdue_workcards": 2,
          "active_personnel": 23,
          "open_ncrs": 3
        },
        "bays": [
          { "bay_no": "机位01", "aircraft_reg": "B-1234", "aircraft_type": "B737-800", "task_no": "TP-2024-001", "check_type": "C检", "progress": 65, "status": "in_progress", "days_remaining": 3, "personnel_count": 5 },
          { "bay_no": "机位02", "aircraft_reg": "B-5678", "aircraft_type": "A320-200", "task_no": "TP-2024-002", "check_type": "D检", "progress": 12, "status": "in_progress", "days_remaining": 15, "personnel_count": 8 },
          { "bay_no": "机位03", "aircraft_reg": "B-9012", "aircraft_type": "B737-MAX8", "task_no": "TP-2024-003", "check_type": "定检", "progress": 90, "status": "in_progress", "days_remaining": 1, "personnel_count": 3 },
          { "bay_no": "机位04", "aircraft_reg": "B-3456", "aircraft_type": "A321-200", "task_no": "TP-2024-004", "check_type": "C检", "progress": 45, "status": "in_progress", "days_remaining": 7, "personnel_count": 6 },
          { "bay_no": "机位05", "aircraft_reg": "B-7890", "aircraft_type": "B787-9", "task_no": "TP-2024-005", "check_type": "D检", "progress": 28, "status": "in_progress", "days_remaining": 20, "personnel_count": 10 },
          { "bay_no": "机位06", "aircraft_reg": null, "task_no": null, "status": "idle", "progress": 0 },
          { "bay_no": "机位07", "aircraft_reg": null, "task_no": null, "status": "idle", "progress": 0 },
          { "bay_no": "机位08", "aircraft_reg": null, "task_no": null, "status": "maintenance", "progress": 0 }
        ],
        "alerts": [
          { "id": "a1", "type": "overdue", "message": "工卡 WC-2024-018 已逾期2天", "workcard_no": "WC-2024-018", "severity": "high", "time": "2024-01-15T08:00:00" },
          { "id": "a2", "type": "pending_quality", "message": "机位03 有3张工卡待质检签署", "severity": "medium", "time": "2024-01-15T09:30:00" },
          { "id": "a3", "type": "license_expiry", "message": "技术员张三的AME执照将于7天后到期", "severity": "medium", "time": "2024-01-15T07:00:00" },
          { "id": "a4", "type": "material_shortage", "message": "工卡 WC-2024-022 所需零件库存不足", "severity": "low", "time": "2024-01-15T10:00:00" }
        ]
      }
    })
  }
]