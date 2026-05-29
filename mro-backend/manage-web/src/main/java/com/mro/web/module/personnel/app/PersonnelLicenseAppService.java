package com.mro.web.module.personnel.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.PersonnelLicenseDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 人员证照应用服务
 * Refs: MRO-009
 */
@Service
@RequiredArgsConstructor
public class PersonnelLicenseAppService {

    @DubboReference(version = "1.0.0")
    private PersonnelLicenseDubboService personnelLicenseDubboService;

    public Long createLicense(CreateLicenseCommand cmd) {
        return personnelLicenseDubboService.createLicense(cmd);
    }

    public void updateLicense(UpdateLicenseCommand cmd) {
        personnelLicenseDubboService.updateLicense(cmd);
    }

    public void deleteLicense(Long id) {
        personnelLicenseDubboService.deleteLicense(id);
    }

    public PersonnelLicenseDTO getLicense(Long id) {
        return personnelLicenseDubboService.getLicense(id);
    }

    public PageResult<PersonnelLicenseDTO> listLicenses(LicenseQueryParam param) {
        return personnelLicenseDubboService.listLicenses(param);
    }

    public void renewLicense(LicenseRenewalCommand cmd) {
        personnelLicenseDubboService.renewLicense(cmd);
    }

    public LicenseStatisticsDTO getStatistics() {
        return personnelLicenseDubboService.getStatistics(null);
    }

    public QualificationCheckResult checkQualification(QualificationCheckParam param) {
        return personnelLicenseDubboService.checkQualification(param);
    }

    public LicenseImportResultDTO importLicenses(MultipartFile file) throws IOException {
        return personnelLicenseDubboService.importLicenses(file.getInputStream());
    }

    public List<LicenseAlertDTO> listAlerts() {
        return personnelLicenseDubboService.listAlerts();
    }

    public String uploadAttachment(Long licenseId, MultipartFile file) {
        // TODO: integrate with object storage (MinIO/OSS); for now return a placeholder URL
        return "/uploads/licenses/" + licenseId + "/" + file.getOriginalFilename();
    }
}
