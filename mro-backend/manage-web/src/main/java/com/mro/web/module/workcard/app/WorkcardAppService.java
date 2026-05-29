package com.mro.web.module.workcard.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.workcard.request.*;
import com.mro.common.dubbo.workcard.response.*;
import com.mro.common.dubbo.workcard.request.*;
import com.mro.common.dubbo.workcard.response.*;
import com.mro.common.dubbo.workcard.service.WorkcardDubboService;
import com.mro.web.annotation.DataScope;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkcardAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private WorkcardDubboService workcardDubboService;

    @DataScope
    public PageResult<WorkcardDTO> listWorkcards(WorkcardQueryParam param) {
        return workcardDubboService.listWorkcards(param, buildCtx());
    }

    public Long createWorkcard(CreateWorkcardCommand cmd) {
        CreateWorkcardCommand withCreator = new CreateWorkcardCommand(
                cmd.title(), cmd.cardType(), cmd.aircraftId(), cmd.priority(),
                cmd.dueDate(), cmd.steps(), UserContext.getUserId());
        return workcardDubboService.createWorkcard(withCreator);
    }

    public WorkcardDetailDTO getWorkcard(Long id) {
        return workcardDubboService.getWorkcard(id);
    }

    public void updateWorkcard(Long id, UpdateWorkcardCommand cmd) {
        UpdateWorkcardCommand withId = new UpdateWorkcardCommand(
                id, cmd.title(), cmd.cardType(), cmd.aircraftId(),
                cmd.priority(), cmd.dueDate(), UserContext.getUserId());
        workcardDubboService.updateWorkcard(withId);
    }

    public void submitForApproval(Long id) {
        workcardDubboService.submitForApproval(id, UserContext.getUserId());
    }

    public void approveWorkcard(Long id, String action, String comment) {
        workcardDubboService.approveWorkcard(id, action, comment, UserContext.getUserId());
    }

    public void issueWorkcard(Long id) {
        workcardDubboService.issueWorkcard(id, UserContext.getUserId());
    }

    public void completeStep(Long workcardId, Long stepId) {
        workcardDubboService.completeStep(workcardId, stepId, UserContext.getUserId());
    }

    public SignatureResultDTO signWorkcard(Long id, SignRequest req) {
        SignWorkcardCommand cmd = new SignWorkcardCommand(
                id, req.stepId(), req.signatureType(), req.digitalSignature(), UserContext.getUserId());
        return workcardDubboService.signWorkcard(cmd);
    }

    public List<SignatureDTO> getSignatures(Long id) {
        return workcardDubboService.getSignatures(id);
    }

    public BlockchainVerifyDTO verifyBlockchain(Long id) {
        return workcardDubboService.verifyBlockchain(id);
    }

    @DataScope
    public WorkcardProgressDTO getProgress() {
        return workcardDubboService.getProgress(buildCtx());
    }

    @DataScope
    public PageResult<WorkcardAlertDTO> getAlerts(int pageNum, int pageSize) {
        return workcardDubboService.getAlerts(new PageParam(pageNum, pageSize), buildCtx());
    }

    private UserContextDTO buildCtx() {
        return new UserContextDTO(UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles(), UserContext.getPermissions());
    }

    public record SignRequest(Long stepId, String signatureType, String digitalSignature) {}
}
