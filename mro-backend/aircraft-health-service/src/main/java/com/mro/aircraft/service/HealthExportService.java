package com.mro.aircraft.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mro.aircraft.domain.entity.FaultRecord;
import com.mro.aircraft.domain.entity.HealthAlert;
import com.mro.aircraft.mapper.FaultRecordMapper;
import com.mro.aircraft.mapper.HealthAlertMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FaultRecordMapper faultRecordMapper;
    private final HealthAlertMapper healthAlertMapper;

    public byte[] exportFaultRecords(String aircraftId) throws IOException {
        List<FaultRecord> records = faultRecordMapper.selectList(
                new LambdaQueryWrapper<FaultRecord>()
                        .eq(aircraftId != null, FaultRecord::getAircraftId, aircraftId)
                        .orderByDesc(FaultRecord::getDetectedAt));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("故障记录");
            String[] headers = {"ID", "飞机注册号", "故障代码", "严重程度", "故障部件", "检测时间", "状态"};
            createHeaderRow(sheet, headers);

            int rowIdx = 1;
            for (FaultRecord r : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getAircraftId());
                row.createCell(2).setCellValue(r.getFaultCode());
                row.createCell(3).setCellValue(r.getSeverity());
                row.createCell(4).setCellValue(r.getComponent());
                row.createCell(5).setCellValue(r.getDetectedAt() != null ? r.getDetectedAt().format(FMT) : "");
                row.createCell(6).setCellValue(r.getStatus());
            }
            return toBytes(workbook);
        }
    }

    public byte[] exportAlerts(String aircraftId) throws IOException {
        List<HealthAlert> alerts = healthAlertMapper.selectList(
                new LambdaQueryWrapper<HealthAlert>()
                        .eq(aircraftId != null, HealthAlert::getAircraftId, aircraftId)
                        .orderByDesc(HealthAlert::getCreatedAt));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("预警记录");
            String[] headers = {"ID", "飞机注册号", "预警等级", "预警内容", "是否已确认", "创建时间"};
            createHeaderRow(sheet, headers);

            int rowIdx = 1;
            for (HealthAlert a : alerts) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getId());
                row.createCell(1).setCellValue(a.getAircraftId());
                row.createCell(2).setCellValue(a.getAlertLevel());
                row.createCell(3).setCellValue(a.getMessage());
                row.createCell(4).setCellValue(Boolean.TRUE.equals(a.getAcknowledged()) ? "是" : "否");
                row.createCell(5).setCellValue(a.getCreatedAt() != null ? a.getCreatedAt().format(FMT) : "");
            }
            return toBytes(workbook);
        }
    }

    private void createHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle style = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] toBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }
}
