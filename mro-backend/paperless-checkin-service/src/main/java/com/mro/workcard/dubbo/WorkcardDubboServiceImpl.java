package com.mro.workcard.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.workcard.request.*;
import com.mro.common.dubbo.workcard.response.*;
import com.mro.common.dubbo.workcard.service.WorkcardDubboService;
import com.mro.workcard.service.QualificationService;
import com.mro.workcard.service.SignatureService;
import com.mro.workcard.service.WorkcardService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 电子工卡核心 Dubbo 实现
 * Refs: MRO-008
 */
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class WorkcardDubboServiceImpl implements WorkcardDubboService {

    private final WorkcardService workcardService;
    private final SignatureService signatureService;
    private final QualificationService qualificationService;

    @Override
    public PageResult<WorkcardDTO> listWorkcards(WorkcardQueryParam param, UserContextDTO ctx) {
        return workcardService.listWorkcards(param);
    }

    @Override
    public Long createWorkcard(CreateWorkcardCommand cmd) {
        return workcardService.createWorkcard(cmd);
    }

    @Override
    public WorkcardDetailDTO getWorkcard(Long workcardId) {
        return workcardService.getWorkcard(workcardId);
    }

    @Override
    public void updateWorkcard(UpdateWorkcardCommand cmd) {
        workcardService.updateWorkcard(cmd);
    }

    @Override
    public void submitForApproval(Long workcardId, Long operatorId) {
        workcardService.submitForApproval(workcardId, operatorId);
    }

    @Override
    public void approveWorkcard(Long workcardId, String action, String comment, Long operatorId) {
        workcardService.approveWorkcard(workcardId, action, comment, operatorId);
    }

    @Override
    public void issueWorkcard(Long workcardId, Long operatorId) {
        workcardService.issueWorkcard(workcardId, operatorId);
    }

    @Override
    public void completeStep(Long workcardId, Long stepId, Long operatorId) {
        workcardService.completeStep(workcardId, stepId, operatorId);
    }

    @Override
    public SignatureResultDTO signWorkcard(SignWorkcardCommand cmd) {
        return signatureService.signWorkcard(cmd);
    }

    @Override
    public List<SignatureDTO> getSignatures(Long workcardId) {
        return workcardService.getSignatures(workcardId);
    }

    @Override
    public BlockchainVerifyDTO verifyBlockchain(Long workcardId) {
        return signatureService.verifyBlockchain(workcardId);
    }

    @Override
    public WorkcardProgressDTO getProgress(UserContextDTO ctx) {
        return workcardService.getProgress();
    }

    @Override
    public PageResult<WorkcardAlertDTO> getAlerts(PageParam param, UserContextDTO ctx) {
        return workcardService.getAlerts(param);
    }

    @Override
    public PageResult<QualificationDTO> listQualifications(int pageNum, int pageSize) {
        return qualificationService.listQualifications(pageNum, pageSize);
    }

    @Override
    public PageResult<QualificationDTO> matchQualifications(QualificationMatchParam param) {
        return qualificationService.matchQualifications(param);
    }
}
