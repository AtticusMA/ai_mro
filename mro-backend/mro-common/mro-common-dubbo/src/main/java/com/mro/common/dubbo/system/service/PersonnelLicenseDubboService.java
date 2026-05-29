package com.mro.common.dubbo.system.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.CreateLicenseCommand;
import com.mro.common.dubbo.system.request.LicenseQueryParam;
import com.mro.common.dubbo.system.request.LicenseRenewalCommand;
import com.mro.common.dubbo.system.request.QualificationCheckParam;
import com.mro.common.dubbo.system.request.UpdateLicenseCommand;
import com.mro.common.dubbo.system.response.LicenseAlertDTO;
import com.mro.common.dubbo.system.response.LicenseImportResultDTO;
import com.mro.common.dubbo.system.response.LicenseStatisticsDTO;
import com.mro.common.dubbo.system.response.PersonnelLicenseDTO;
import com.mro.common.dubbo.system.response.QualificationCheckResult;

import java.io.InputStream;
import java.util.List;

public interface PersonnelLicenseDubboService {

    Long createLicense(CreateLicenseCommand cmd);

    void updateLicense(UpdateLicenseCommand cmd);

    void deleteLicense(Long id);

    PersonnelLicenseDTO getLicense(Long id);

    PageResult<PersonnelLicenseDTO> listLicenses(LicenseQueryParam param);

    void renewLicense(LicenseRenewalCommand cmd);

    LicenseStatisticsDTO getStatistics(Long userId);

    QualificationCheckResult checkQualification(QualificationCheckParam param);

    List<LicenseAlertDTO> listAlerts();

    List<PersonnelLicenseDTO> listByUserId(Long userId);

    LicenseImportResultDTO importLicenses(InputStream inputStream);
}
