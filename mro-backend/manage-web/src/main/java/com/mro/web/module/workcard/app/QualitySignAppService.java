package com.mro.web.module.workcard.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.workcard.request.QualitySignCommand;
import com.mro.common.dubbo.workcard.request.WorkcardQueryParam;
import com.mro.common.dubbo.workcard.response.QualitySignRecordDTO;
import com.mro.common.dubbo.workcard.response.WorkcardDTO;
import com.mro.common.dubbo.workcard.response.WorkcardDetailDTO;
import com.mro.common.dubbo.workcard.service.QualitySignDubboService;
import com.mro.common.dubbo.workcard.service.WorkcardDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 质检签署应用服务
 * Refs: MRO-008
 */
@Service
@RequiredArgsConstructor
public class QualitySignAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private QualitySignDubboService qualitySignDubboService;

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private WorkcardDubboService workcardDubboService;

    public PageResult<WorkcardDTO> listPendingSign(int pageNum, int pageSize) {
        WorkcardQueryParam param = new WorkcardQueryParam("completed", null, null, null, pageNum, pageSize);
        UserContextDTO ctx = new UserContextDTO(UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles(), UserContext.getPermissions());
        return workcardDubboService.listWorkcards(param, ctx);
    }

    public WorkcardDetailDTO getQualitySignDetail(Long id) {
        return workcardDubboService.getWorkcard(id);
    }

    public Long submitQualitySign(Long workcardId, QualitySignCommand cmd) {
        QualitySignCommand withId = new QualitySignCommand(
                workcardId, cmd.stepId(), cmd.result(), cmd.comment(),
                cmd.signTime(), cmd.signatureHash());
        return qualitySignDubboService.qualitySign(withId, UserContext.getUserId());
    }

    public List<QualitySignRecordDTO> listSignRecords(Long workcardId) {
        return qualitySignDubboService.listSignRecords(workcardId);
    }

    public QualitySignRecordDTO getSignRecord(Long id) {
        return qualitySignDubboService.getSignRecord(id);
    }
}
