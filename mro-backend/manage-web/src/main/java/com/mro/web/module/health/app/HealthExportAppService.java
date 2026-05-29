package com.mro.web.module.health.app;

import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.health.request.*;
import com.mro.common.dubbo.health.response.*;
import com.mro.common.dubbo.health.service.HealthDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthExportAppService {

    private static final int PAGE_SIZE = 1000;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    @DubboReference(version = "1.0.0", timeout = 10000, retries = 0)
    private HealthDubboService healthDubboService;

    public byte[] exportFaults(String aircraftId) throws IOException {
        List<FaultRecordDTO> records = healthDubboService
                .listFaults(aircraftId, new FaultQueryParam(1, PAGE_SIZE, null, null))
                .list();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("故障记录");
            String[] headers = {"ID", "飞机注册号", "故障代码", "严重程度", "故障部件", "检测时间", "状态"};
            createHeaderRow(workbook, sheet, headers);

            int rowIdx = 1;
            for (FaultRecordDTO r : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.id());
                row.createCell(1).setCellValue(aircraftId != null ? aircraftId : "");
                row.createCell(2).setCellValue(r.faultCode());
                row.createCell(3).setCellValue(r.severity());
                row.createCell(4).setCellValue(r.component());
                row.createCell(5).setCellValue(r.detectedAt() != null ? FMT.format(r.detectedAt()) : "");
                row.createCell(6).setCellValue(r.status());
            }
            return toBytes(workbook);
        }
    }

    public byte[] exportAlerts(String aircraftId) throws IOException {
        List<HealthAlertDTO> alerts = healthDubboService
                .listAlerts(new AlertQueryParam(1, PAGE_SIZE, null, null, aircraftId), null)
                .list();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("预警记录");
            String[] headers = {"ID", "飞机注册号", "预警等级", "预警内容", "是否已确认", "创建时间"};
            createHeaderRow(workbook, sheet, headers);

            int rowIdx = 1;
            for (HealthAlertDTO a : alerts) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.id());
                row.createCell(1).setCellValue(a.aircraftId());
                row.createCell(2).setCellValue(a.alertLevel());
                row.createCell(3).setCellValue(a.message());
                row.createCell(4).setCellValue(a.acknowledged() ? "是" : "否");
                row.createCell(5).setCellValue(a.createdAt() != null ? FMT.format(a.createdAt()) : "");
            }
            return toBytes(workbook);
        }
    }

    private void createHeaderRow(Workbook workbook, Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
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
