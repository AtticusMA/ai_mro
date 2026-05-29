package com.mro.ar.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.ar.domain.entity.AnomalyRecord;
import com.mro.ar.domain.entity.InspectionTask;
import com.mro.ar.mapper.AnomalyRecordMapper;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.ar.response.AnomalyRecordDTO;
import com.mro.common.dubbo.common.request.HealthPageParam;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyService {

    private final AnomalyRecordMapper anomalyRecordMapper;
    private final InspectionService inspectionService;
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    public PageResult<AnomalyRecordDTO> listAnomalies(Long taskId, HealthPageParam param) {
        inspectionService.getTaskOrThrow(taskId);

        Page<AnomalyRecord> page = anomalyRecordMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()),
                new LambdaQueryWrapper<AnomalyRecord>()
                        .eq(AnomalyRecord::getTaskId, taskId)
                        .orderByDesc(AnomalyRecord::getDetectedAt));

        return PageResult.of(page.getRecords().stream()
                .map(this::toDTO).toList(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * Called by AI edge node to report an anomaly.
     * snapshotBase64 is optional base64-encoded JPEG; if provided it is stored in MinIO.
     */
    @Transactional
    public Long reportAnomaly(Long taskId, String anomalyType, double confidence, String snapshotBase64) {
        InspectionTask task = inspectionService.getTaskOrThrow(taskId);
        if (!"in_progress".equals(task.getStatus())) {
            throw new BizException(4301, "巡检任务未在进行中");
        }

        String snapshotUrl = null;
        if (snapshotBase64 != null && !snapshotBase64.isBlank()) {
            snapshotUrl = uploadSnapshot(snapshotBase64);
        }

        AnomalyRecord record = new AnomalyRecord();
        record.setTaskId(taskId);
        record.setAnomalyType(anomalyType);
        record.setConfidence(new java.math.BigDecimal(String.valueOf(confidence)));
        record.setSnapshotUrl(snapshotUrl);
        record.setDetectedAt(LocalDateTime.now());
        anomalyRecordMapper.insert(record);

        log.info("Anomaly recorded: taskId={}, type={}, confidence={}", taskId, anomalyType, confidence);
        return record.getId();
    }

    private String uploadSnapshot(String base64) {
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            String objectName = "snapshots/" + UUID.randomUUID() + ".jpg";
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .contentType("image/jpeg")
                    .build());
            return minioEndpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            log.error("Failed to upload snapshot to MinIO: {}", e.getMessage());
            return null;
        }
    }

    private AnomalyRecordDTO toDTO(AnomalyRecord r) {
        return new AnomalyRecordDTO(
                r.getId(),
                r.getTaskId(),
                r.getAnomalyType(),
                r.getConfidence(),
                r.getSnapshotUrl(),
                r.getDetectedAt() != null ? r.getDetectedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}
