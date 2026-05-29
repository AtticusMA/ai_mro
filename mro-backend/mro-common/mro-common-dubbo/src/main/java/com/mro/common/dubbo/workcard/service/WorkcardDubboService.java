package com.mro.common.dubbo.workcard.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.workcard.request.*;
import com.mro.common.dubbo.workcard.response.*;

import java.util.List;

/**
 * 电子工卡核心 Dubbo 接口（编制 / 审批 / 下发 / 步骤 / 签署 / 进度 / 预警 / 资质）
 * Refs: MRO-008
 */
public interface WorkcardDubboService {

    PageResult<WorkcardDTO> listWorkcards(WorkcardQueryParam param, UserContextDTO ctx);

    Long createWorkcard(CreateWorkcardCommand cmd);

    WorkcardDetailDTO getWorkcard(Long workcardId);

    void updateWorkcard(UpdateWorkcardCommand cmd);

    void submitForApproval(Long workcardId, Long operatorId);

    void approveWorkcard(Long workcardId, String action, String comment, Long operatorId);

    void issueWorkcard(Long workcardId, Long operatorId);

    void completeStep(Long workcardId, Long stepId, Long operatorId);

    SignatureResultDTO signWorkcard(SignWorkcardCommand cmd);

    List<SignatureDTO> getSignatures(Long workcardId);

    BlockchainVerifyDTO verifyBlockchain(Long workcardId);

    WorkcardProgressDTO getProgress(UserContextDTO ctx);

    PageResult<WorkcardAlertDTO> getAlerts(PageParam param, UserContextDTO ctx);

    PageResult<QualificationDTO> listQualifications(int pageNum, int pageSize);

    PageResult<QualificationDTO> matchQualifications(QualificationMatchParam param);
}
