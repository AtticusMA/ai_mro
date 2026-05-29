package com.mro.workcard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mro.common.core.exception.BizException;
import com.mro.common.dubbo.workcard.request.QualitySignCommand;
import com.mro.common.dubbo.workcard.response.QualitySignRecordDTO;
import com.mro.workcard.domain.entity.Ncr;
import com.mro.workcard.domain.entity.QualitySignRecord;
import com.mro.workcard.domain.entity.Workcard;
import com.mro.workcard.mapper.NcrMapper;
import com.mro.workcard.mapper.QualitySignRecordMapper;
import com.mro.workcard.mapper.WorkcardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualitySignService {

    private static final int ERR_SIGN_RECORD_NOT_FOUND = 4901;

    private final QualitySignRecordMapper qualitySignRecordMapper;
    private final NcrMapper ncrMapper;
    private final WorkcardMapper workcardMapper;

    @Transactional
    public Long qualitySign(QualitySignCommand cmd, Long signerId) {
        QualitySignRecord record = new QualitySignRecord();
        record.setWorkcardId(cmd.workcardId());
        record.setStepId(cmd.stepId());
        record.setSignerId(signerId);
        record.setResult(cmd.result());
        record.setComment(cmd.comment());
        record.setSignTime(cmd.signTime() != null ? cmd.signTime() : LocalDateTime.now());
        record.setSignatureHash(cmd.signatureHash());
        qualitySignRecordMapper.insert(record);

        if ("fail".equals(cmd.result())) {
            Ncr ncr = new Ncr();
            ncr.setWorkcardId(cmd.workcardId());
            ncr.setQualitySignId(record.getId());
            ncr.setNcrNo(String.format("NCR-%s-%04d",
                    LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                    ThreadLocalRandom.current().nextInt(1000, 9999)));
            ncr.setTitle("质检不合格-工卡" + cmd.workcardId());
            ncr.setStatus("open");
            ncr.setCreatedBy(signerId);
            ncrMapper.insert(ncr);
        } else if ("pass".equals(cmd.result())) {
            Workcard workcard = workcardMapper.selectById(cmd.workcardId());
            if (workcard != null) {
                workcard.setStatus("archived");
                workcardMapper.updateById(workcard);
            }
        }

        return record.getId();
    }

    public List<QualitySignRecordDTO> listSignRecords(Long workcardId) {
        List<QualitySignRecord> records = qualitySignRecordMapper.selectList(
                new LambdaQueryWrapper<QualitySignRecord>()
                        .eq(QualitySignRecord::getWorkcardId, workcardId)
                        .orderByDesc(QualitySignRecord::getSignTime));
        return records.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public QualitySignRecordDTO getSignRecord(Long id) {
        QualitySignRecord record = qualitySignRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException(ERR_SIGN_RECORD_NOT_FOUND, "质检记录不存在");
        }
        return toDTO(record);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private QualitySignRecordDTO toDTO(QualitySignRecord r) {
        return new QualitySignRecordDTO(
                r.getId(), r.getWorkcardId(), r.getStepId(), r.getSignerId(),
                null, r.getResult(), r.getComment(), r.getSignTime(), r.getSignatureHash());
    }
}
