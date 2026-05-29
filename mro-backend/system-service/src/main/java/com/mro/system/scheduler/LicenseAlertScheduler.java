package com.mro.system.scheduler;

import com.mro.common.dubbo.system.response.LicenseAlertDTO;
import com.mro.system.service.PersonnelLicenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 证照到期预警定时任务。
 * 每日08:00执行，检查即将到期/已过期证照并记录日志。
 * 后续将集成通知系统（站内信/邮件）。
 *
 * Refs: MRO-009 / T-005
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LicenseAlertScheduler {

    private final PersonnelLicenseService personnelLicenseService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void checkLicenseExpiry() {
        List<LicenseAlertDTO> alerts = personnelLicenseService.listAlerts();
        if (alerts.isEmpty()) {
            log.info("证照到期检查完成，无预警");
            return;
        }
        for (LicenseAlertDTO alert : alerts) {
            if ("urgent".equals(alert.alertLevel())) {
                log.warn("【紧急】证照即将到期: userId={}, licenseNo={}, 剩余{}天",
                        alert.userId(), alert.licenseNo(), alert.daysRemaining());
            } else {
                log.info("【预警】证照即将到期: userId={}, licenseNo={}, 剩余{}天",
                        alert.userId(), alert.licenseNo(), alert.daysRemaining());
            }
        }
        log.info("证照到期检查完成，共{}条预警", alerts.size());
    }
}
