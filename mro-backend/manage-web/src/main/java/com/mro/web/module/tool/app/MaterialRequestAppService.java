package com.mro.web.module.tool.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.material.request.*;
import com.mro.common.dubbo.material.response.*;
import com.mro.common.dubbo.material.service.MaterialRequestDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * 航材领料申请应用服务
 * Refs: MRO-006
 */
@Service
@RequiredArgsConstructor
public class MaterialRequestAppService {

    @DubboReference(version = "1.0.0")
    private MaterialRequestDubboService materialRequestDubboService;

    public WorkcardBomDTO getWorkcardBom(Long workcardId) {
        return materialRequestDubboService.getWorkcardBom(workcardId);
    }

    public Long createMaterialRequest(CreateMaterialRequestCommand cmd) {
        Long userId = UserContext.getUserId();
        return materialRequestDubboService.createMaterialRequest(new CreateMaterialRequestCommand(
                cmd.workcardId(), userId, cmd.deptId(), cmd.urgency(), cmd.items()));
    }

    public PageResult<MaterialRequestDTO> listMaterialRequests(Long workcardId, String status, int pageNum, int pageSize) {
        return materialRequestDubboService.listMaterialRequests(
                new MaterialRequestQueryParam(pageNum, pageSize, workcardId, status, null));
    }

    public MaterialRequestDetailDTO getMaterialRequest(Long id) {
        return materialRequestDubboService.getMaterialRequest(id);
    }

    public void approveMaterialRequest(Long id) {
        materialRequestDubboService.approveMaterialRequest(id, UserContext.getUserId());
    }

    public void rejectMaterialRequest(Long id, String rejectReason) {
        materialRequestDubboService.rejectMaterialRequest(id, rejectReason, UserContext.getUserId());
    }

    public void receiveMaterialRequest(Long id) {
        materialRequestDubboService.receiveMaterialRequest(id, UserContext.getUserId());
    }
}
