package com.mro.workcard.dubbo;

import com.mro.common.dubbo.workcard.request.QualitySignCommand;
import com.mro.common.dubbo.workcard.response.QualitySignRecordDTO;
import com.mro.common.dubbo.workcard.service.QualitySignDubboService;
import com.mro.workcard.service.QualitySignService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 质检签署 Dubbo 实现
 * Refs: MRO-008
 */
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class QualitySignDubboServiceImpl implements QualitySignDubboService {

    private final QualitySignService qualitySignService;

    @Override
    public Long qualitySign(QualitySignCommand cmd, Long signerId) {
        return qualitySignService.qualitySign(cmd, signerId);
    }

    @Override
    public List<QualitySignRecordDTO> listSignRecords(Long workcardId) {
        return qualitySignService.listSignRecords(workcardId);
    }

    @Override
    public QualitySignRecordDTO getSignRecord(Long id) {
        return qualitySignService.getSignRecord(id);
    }
}
