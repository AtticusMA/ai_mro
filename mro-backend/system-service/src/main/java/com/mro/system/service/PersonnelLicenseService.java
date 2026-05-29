package com.mro.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.CreateLicenseCommand;
import com.mro.common.dubbo.system.response.LicenseAlertDTO;
import com.mro.common.dubbo.system.request.LicenseQueryParam;
import com.mro.common.dubbo.system.request.LicenseRenewalCommand;
import com.mro.common.dubbo.system.response.LicenseStatisticsDTO;
import com.mro.common.dubbo.system.response.PersonnelLicenseDTO;
import com.mro.common.dubbo.system.request.QualificationCheckParam;
import com.mro.common.dubbo.system.response.QualificationCheckResult;
import com.mro.common.dubbo.system.request.UpdateLicenseCommand;
import com.mro.system.entity.PersonnelLicense;
import com.mro.system.mapper.PersonnelLicenseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 人员证照管理服务
 * Refs: MRO-009
 */
@Service
@RequiredArgsConstructor
public class PersonnelLicenseService {

    private static final int ERR_LICENSE_NOT_FOUND = 5001;

    private final PersonnelLicenseMapper personnelLicenseMapper;

    // ==================== 1. createLicense ====================

    @Transactional(rollbackFor = Exception.class)
    public Long createLicense(CreateLicenseCommand cmd) {
        PersonnelLicense entity = new PersonnelLicense();
        entity.setUserId(cmd.userId());
        entity.setLicenseNo(cmd.licenseNo());
        entity.setLicenseType(cmd.licenseType());
        entity.setAircraftType(cmd.aircraftType());
        entity.setCategory(cmd.category());
        entity.setIssuer(cmd.issuer());
        entity.setIssueDate(cmd.issueDate());
        entity.setExpiryDate(cmd.expiryDate());
        entity.setFileUrl(cmd.fileUrl());
        entity.setRemark(cmd.remark());
        entity.setStatus(computeStatus(cmd.expiryDate()));
        personnelLicenseMapper.insert(entity);
        return entity.getId();
    }

    // ==================== 2. updateLicense ====================

    @Transactional(rollbackFor = Exception.class)
    public void updateLicense(UpdateLicenseCommand cmd) {
        PersonnelLicense entity = personnelLicenseMapper.selectById(cmd.id());
        if (entity == null) {
            throw new BizException(ERR_LICENSE_NOT_FOUND, "证照不存在");
        }
        if (cmd.licenseNo() != null) entity.setLicenseNo(cmd.licenseNo());
        if (cmd.licenseType() != null) entity.setLicenseType(cmd.licenseType());
        if (cmd.aircraftType() != null) entity.setAircraftType(cmd.aircraftType());
        if (cmd.category() != null) entity.setCategory(cmd.category());
        if (cmd.issuer() != null) entity.setIssuer(cmd.issuer());
        if (cmd.issueDate() != null) entity.setIssueDate(cmd.issueDate());
        if (cmd.expiryDate() != null) {
            entity.setExpiryDate(cmd.expiryDate());
        }
        if (cmd.fileUrl() != null) entity.setFileUrl(cmd.fileUrl());
        if (cmd.remark() != null) entity.setRemark(cmd.remark());
        entity.setStatus(computeStatus(entity.getExpiryDate()));
        personnelLicenseMapper.updateById(entity);
    }

    // ==================== 3. deleteLicense ====================

    @Transactional(rollbackFor = Exception.class)
    public void deleteLicense(Long id) {
        PersonnelLicense entity = personnelLicenseMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ERR_LICENSE_NOT_FOUND, "证照不存在");
        }
        personnelLicenseMapper.deleteById(id);
    }

    // ==================== 4. getLicense ====================

    public PersonnelLicenseDTO getLicense(Long id) {
        PersonnelLicense entity = personnelLicenseMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ERR_LICENSE_NOT_FOUND, "证照不存在");
        }
        return toDTO(entity);
    }

    // ==================== 5. listLicenses ====================

    public PageResult<PersonnelLicenseDTO> listLicenses(LicenseQueryParam param) {
        Page<PersonnelLicense> page = new Page<>(param.pageNum(), param.pageSize());
        LambdaQueryWrapper<PersonnelLicense> wrapper = new LambdaQueryWrapper<>();

        if (param.userId() != null) {
            wrapper.eq(PersonnelLicense::getUserId, param.userId());
        }
        if (StringUtils.hasText(param.licenseType())) {
            wrapper.eq(PersonnelLicense::getLicenseType, param.licenseType());
        }
        if (StringUtils.hasText(param.aircraftType())) {
            wrapper.eq(PersonnelLicense::getAircraftType, param.aircraftType());
        }

        // Status filter — translated to date-based SQL conditions since status is dynamic
        if (StringUtils.hasText(param.status())) {
            LocalDate today = LocalDate.now();
            switch (param.status()) {
                case "expired" -> wrapper.lt(PersonnelLicense::getExpiryDate, today);
                case "expiring" -> wrapper.ge(PersonnelLicense::getExpiryDate, today)
                        .le(PersonnelLicense::getExpiryDate, today.plusDays(30));
                case "valid" -> wrapper.gt(PersonnelLicense::getExpiryDate, today.plusDays(30));
                default -> { /* ignore unknown status values */ }
            }
        }

        wrapper.orderByDesc(PersonnelLicense::getCreateTime);
        Page<PersonnelLicense> result = personnelLicenseMapper.selectPage(page, wrapper);

        List<PersonnelLicenseDTO> records = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageResult<>(records, result.getTotal(), param.pageNum(), param.pageSize());
    }

    // ==================== 6. renewLicense ====================

    @Transactional(rollbackFor = Exception.class)
    public void renewLicense(LicenseRenewalCommand cmd) {
        PersonnelLicense entity = personnelLicenseMapper.selectById(cmd.licenseId());
        if (entity == null) {
            throw new BizException(ERR_LICENSE_NOT_FOUND, "证照不存在");
        }
        entity.setExpiryDate(cmd.newExpiryDate());
        if (StringUtils.hasText(cmd.newLicenseNo())) {
            entity.setLicenseNo(cmd.newLicenseNo());
        }
        if (StringUtils.hasText(cmd.fileUrl())) {
            entity.setFileUrl(cmd.fileUrl());
        }
        entity.setStatus(computeStatus(cmd.newExpiryDate()));
        personnelLicenseMapper.updateById(entity);
    }

    // ==================== 7. getStatistics ====================

    public LicenseStatisticsDTO getStatistics(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(30);

        LambdaQueryWrapper<PersonnelLicense> baseWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            baseWrapper.eq(PersonnelLicense::getUserId, userId);
        }

        long total = personnelLicenseMapper.selectCount(new LambdaQueryWrapper<PersonnelLicense>()
                .eq(userId != null, PersonnelLicense::getUserId, userId));

        long validCount = personnelLicenseMapper.selectCount(new LambdaQueryWrapper<PersonnelLicense>()
                .eq(userId != null, PersonnelLicense::getUserId, userId)
                .gt(PersonnelLicense::getExpiryDate, threshold));

        long expiringCount = personnelLicenseMapper.selectCount(new LambdaQueryWrapper<PersonnelLicense>()
                .eq(userId != null, PersonnelLicense::getUserId, userId)
                .ge(PersonnelLicense::getExpiryDate, today)
                .le(PersonnelLicense::getExpiryDate, threshold));

        long expiredCount = personnelLicenseMapper.selectCount(new LambdaQueryWrapper<PersonnelLicense>()
                .eq(userId != null, PersonnelLicense::getUserId, userId)
                .lt(PersonnelLicense::getExpiryDate, today));

        return new LicenseStatisticsDTO(total, validCount, expiringCount, expiredCount);
    }

    // ==================== 8. checkQualification ====================

    public QualificationCheckResult checkQualification(QualificationCheckParam param) {
        LocalDate today = LocalDate.now();

        LambdaQueryWrapper<PersonnelLicense> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonnelLicense::getUserId, param.userId())
                .eq(PersonnelLicense::getAircraftType, param.aircraftType())
                .eq(PersonnelLicense::getCategory, param.category())
                .gt(PersonnelLicense::getExpiryDate, today)
                .last("LIMIT 1");

        PersonnelLicense license = personnelLicenseMapper.selectOne(wrapper);
        if (license != null) {
            return new QualificationCheckResult(true, license.getLicenseNo(), license.getExpiryDate(), null);
        }
        return new QualificationCheckResult(false, null, null, "未找到有效的对应证照");
    }

    // ==================== 9. listAlerts ====================

    public List<LicenseAlertDTO> listAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate alertStart = today.minusDays(7);  // include recently expired (past 7 days)
        LocalDate alertEnd = today.plusDays(30);     // expiring within 30 days

        LambdaQueryWrapper<PersonnelLicense> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(PersonnelLicense::getExpiryDate, alertStart)
                .le(PersonnelLicense::getExpiryDate, alertEnd)
                .orderByAsc(PersonnelLicense::getExpiryDate);

        List<PersonnelLicense> licenses = personnelLicenseMapper.selectList(wrapper);

        return licenses.stream().map(license -> {
            int daysRemaining = (int) ChronoUnit.DAYS.between(today, license.getExpiryDate());
            String alertLevel = daysRemaining <= 7 ? "urgent" : "warning";
            return new LicenseAlertDTO(
                    license.getId(),
                    license.getUserId(),
                    null, // userName not stored in entity, resolved at API layer if needed
                    license.getLicenseNo(),
                    license.getLicenseType(),
                    license.getExpiryDate(),
                    daysRemaining,
                    alertLevel
            );
        }).collect(Collectors.toList());
    }

    // ==================== 10. listByUserId ====================

    public List<PersonnelLicenseDTO> listByUserId(Long userId) {
        LambdaQueryWrapper<PersonnelLicense> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonnelLicense::getUserId, userId)
                .orderByDesc(PersonnelLicense::getCreateTime);

        List<PersonnelLicense> licenses = personnelLicenseMapper.selectList(wrapper);
        return licenses.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== Helper methods ====================

    private String computeStatus(LocalDate expiryDate) {
        if (expiryDate == null) {
            return "valid";
        }
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) return "expired";
        if (!expiryDate.isAfter(today.plusDays(30))) return "expiring";
        return "valid";
    }

    private PersonnelLicenseDTO toDTO(PersonnelLicense entity) {
        return new PersonnelLicenseDTO(
                entity.getId(),
                entity.getUserId(),
                null, // userName resolved at API layer
                entity.getLicenseNo(),
                entity.getLicenseType(),
                entity.getAircraftType(),
                entity.getCategory(),
                entity.getIssuer(),
                entity.getIssueDate(),
                entity.getExpiryDate(),
                computeStatus(entity.getExpiryDate()),
                entity.getFileUrl(),
                entity.getRemark(),
                entity.getCreateTime()
        );
    }
}
