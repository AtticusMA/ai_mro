package com.mro.system.service;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * EasyExcel row mapping class for license batch import.
 * Mutable POJO required by EasyExcel (records are immutable).
 * Refs: MRO-009 / T-006
 */
@Data
public class LicenseImportRowData {

    @ExcelProperty("人员ID")
    private Long userId;

    @ExcelProperty("证照编号")
    private String licenseNo;

    @ExcelProperty("证照类型")
    private String licenseType;

    @ExcelProperty("适用机型")
    private String aircraftType;

    @ExcelProperty("类别")
    private String category;

    @ExcelProperty("发证机构")
    private String issuer;

    @ExcelProperty("签发日期")
    private LocalDate issueDate;

    @ExcelProperty("到期日期")
    private LocalDate expiryDate;

    @ExcelProperty("备注")
    private String remark;
}
