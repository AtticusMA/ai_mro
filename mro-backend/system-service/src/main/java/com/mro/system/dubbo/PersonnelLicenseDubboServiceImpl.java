package com.mro.system.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.PersonnelLicenseDubboService;
import com.mro.system.service.LicenseImportService;
import com.mro.system.service.PersonnelLicenseService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.List;

/**
 * 人员证照管理 Dubbo 接口实现
 * Refs: MRO-009 / T-007
 */
@DubboService
public class PersonnelLicenseDubboServiceImpl implements PersonnelLicenseDubboService {

    @Autowired
    private PersonnelLicenseService personnelLicenseService;

    @Autowired
    private LicenseImportService licenseImportService;

    @Override
    public Long createLicense(CreateLicenseCommand cmd) {
        return personnelLicenseService.createLicense(cmd);
    }

    @Override
    public void updateLicense(UpdateLicenseCommand cmd) {
        personnelLicenseService.updateLicense(cmd);
    }

    @Override
    public void deleteLicense(Long id) {
        personnelLicenseService.deleteLicense(id);
    }

    @Override
    public PersonnelLicenseDTO getLicense(Long id) {
        return personnelLicenseService.getLicense(id);
    }

    @Override
    public PageResult<PersonnelLicenseDTO> listLicenses(LicenseQueryParam param) {
        return personnelLicenseService.listLicenses(param);
    }

    @Override
    public void renewLicense(LicenseRenewalCommand cmd) {
        personnelLicenseService.renewLicense(cmd);
    }

    @Override
    public LicenseStatisticsDTO getStatistics(Long userId) {
        return personnelLicenseService.getStatistics(userId);
    }

    @Override
    public QualificationCheckResult checkQualification(QualificationCheckParam param) {
        return personnelLicenseService.checkQualification(param);
    }

    @Override
    public List<LicenseAlertDTO> listAlerts() {
        return personnelLicenseService.listAlerts();
    }

    @Override
    public List<PersonnelLicenseDTO> listByUserId(Long userId) {
        return personnelLicenseService.listByUserId(userId);
    }

    @Override
    public LicenseImportResultDTO importLicenses(InputStream inputStream) {
        return licenseImportService.importLicenses(inputStream);
    }
}
