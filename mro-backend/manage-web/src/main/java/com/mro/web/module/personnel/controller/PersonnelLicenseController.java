package com.mro.web.module.personnel.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.web.module.personnel.app.PersonnelLicenseAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 人员证照管理接口
 * Refs: MRO-009
 */
@RestController
@RequestMapping("/api/personnel/licenses")
@Validated
@RequiredArgsConstructor
public class PersonnelLicenseController {

    private final PersonnelLicenseAppService personnelLicenseAppService;

    @PostMapping
    public R<Long> createLicense(@RequestBody CreateLicenseCommand cmd) {
        return R.ok(personnelLicenseAppService.createLicense(cmd));
    }

    @PutMapping("/{id}")
    public R<Void> updateLicense(@PathVariable Long id, @RequestBody UpdateLicenseCommand cmd) {
        UpdateLicenseCommand withId = new UpdateLicenseCommand(
                id, cmd.licenseNo(), cmd.licenseType(), cmd.aircraftType(), cmd.category(),
                cmd.issuer(), cmd.issueDate(), cmd.expiryDate(), cmd.fileUrl(), cmd.remark());
        personnelLicenseAppService.updateLicense(withId);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteLicense(@PathVariable Long id) {
        personnelLicenseAppService.deleteLicense(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<PersonnelLicenseDTO> getLicense(@PathVariable Long id) {
        return R.ok(personnelLicenseAppService.getLicense(id));
    }

    @GetMapping
    public R<PageResult<PersonnelLicenseDTO>> listLicenses(LicenseQueryParam param) {
        return R.ok(personnelLicenseAppService.listLicenses(param));
    }

    @PostMapping("/{id}/renew")
    public R<Void> renewLicense(@PathVariable Long id, @RequestBody LicenseRenewalCommand cmd) {
        LicenseRenewalCommand withId = new LicenseRenewalCommand(
                id, cmd.newExpiryDate(), cmd.newLicenseNo(), cmd.fileUrl());
        personnelLicenseAppService.renewLicense(withId);
        return R.ok();
    }

    @GetMapping("/alerts")
    public R<List<LicenseAlertDTO>> listAlerts() {
        return R.ok(personnelLicenseAppService.listAlerts());
    }

    @GetMapping("/stats")
    public R<LicenseStatisticsDTO> getStats() {
        return R.ok(personnelLicenseAppService.getStatistics());
    }

    @PostMapping("/check-qualification")
    public R<QualificationCheckResult> checkQualification(@RequestBody QualificationCheckParam param) {
        return R.ok(personnelLicenseAppService.checkQualification(param));
    }

    @PostMapping("/import")
    public R<LicenseImportResultDTO> importLicenses(@RequestParam("file") MultipartFile file) throws IOException {
        return R.ok(personnelLicenseAppService.importLicenses(file));
    }

    @PostMapping("/{id}/attachment")
    public R<Map<String, String>> uploadAttachment(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String fileUrl = personnelLicenseAppService.uploadAttachment(id, file);
        return R.ok(Map.of("attachmentUrl", fileUrl));
    }
}
