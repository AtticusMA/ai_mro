package com.mro.system.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.mro.common.dubbo.system.request.CreateLicenseCommand;
import com.mro.common.dubbo.system.response.LicenseImportResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 人员证照批量导入服务 — 使用 EasyExcel 解析 Excel 文件并逐行创建证照。
 * Refs: MRO-009 / T-006
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseImportService {

    private final PersonnelLicenseService personnelLicenseService;

    /**
     * 从 Excel InputStream 批量导入证照。
     *
     * @param inputStream 上传的 Excel 文件流
     * @return 导入结果统计
     */
    public LicenseImportResultDTO importLicenses(InputStream inputStream) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> errors = new ArrayList<>();

        EasyExcel.read(inputStream, LicenseImportRowData.class,
                new PageReadListener<LicenseImportRowData>(dataList -> {
                    for (LicenseImportRowData row : dataList) {
                        try {
                            CreateLicenseCommand cmd = new CreateLicenseCommand(
                                    row.getUserId(),
                                    row.getLicenseNo(),
                                    row.getLicenseType(),
                                    row.getAircraftType(),
                                    row.getCategory(),
                                    row.getIssuer(),
                                    row.getIssueDate(),
                                    row.getExpiryDate(),
                                    null,  // fileUrl — not available in spreadsheet import
                                    row.getRemark()
                            );
                            personnelLicenseService.createLicense(cmd);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                            int rowNum = successCount.get() + failCount.get();
                            errors.add(String.format("行%d: %s", rowNum, e.getMessage()));
                            log.warn("导入证照失败: userId={}, licenseNo={}, error={}",
                                    row.getUserId(), row.getLicenseNo(), e.getMessage());
                        }
                    }
                })).sheet().doRead();

        int total = successCount.get() + failCount.get();
        String errorDetails = errors.isEmpty() ? null : String.join("; ", errors);
        return new LicenseImportResultDTO(total, successCount.get(), failCount.get(), errorDetails);
    }
}
