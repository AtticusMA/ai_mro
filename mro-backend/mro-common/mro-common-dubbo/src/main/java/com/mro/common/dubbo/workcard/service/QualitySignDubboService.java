package com.mro.common.dubbo.workcard.service;

import com.mro.common.dubbo.workcard.request.QualitySignCommand;
import com.mro.common.dubbo.workcard.response.QualitySignRecordDTO;

import java.util.List;

/**
 * 质检签署 Dubbo 接口
 * Refs: MRO-008
 */
public interface QualitySignDubboService {

    Long qualitySign(QualitySignCommand cmd, Long signerId);

    List<QualitySignRecordDTO> listSignRecords(Long workcardId);

    QualitySignRecordDTO getSignRecord(Long id);
}
