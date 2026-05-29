package com.mro.tool.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.material.request.CreateMaterialRequestCommand;
import com.mro.common.dubbo.material.request.MaterialRequestQueryParam;
import com.mro.common.dubbo.material.response.MaterialRequestDTO;
import com.mro.common.dubbo.material.response.MaterialRequestDetailDTO;
import com.mro.common.dubbo.material.response.WorkcardBomDTO;
import com.mro.common.dubbo.material.service.MaterialRequestDubboService;
import com.mro.tool.service.MaterialRequestService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 航材领料申请 Dubbo 实现
 * Refs: MRO-006
 */
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class MaterialRequestDubboServiceImpl implements MaterialRequestDubboService {

    private final MaterialRequestService materialRequestService;

    @Override
    public WorkcardBomDTO getWorkcardBom(Long workcardId) {
        return materialRequestService.getWorkcardBom(workcardId);
    }

    @Override
    public Long createMaterialRequest(CreateMaterialRequestCommand cmd) {
        return materialRequestService.createMaterialRequest(cmd);
    }

    @Override
    public PageResult<MaterialRequestDTO> listMaterialRequests(MaterialRequestQueryParam param) {
        return materialRequestService.listMaterialRequests(param);
    }

    @Override
    public MaterialRequestDetailDTO getMaterialRequest(Long id) {
        return materialRequestService.getMaterialRequest(id);
    }

    @Override
    public void approveMaterialRequest(Long id, Long approverId) {
        materialRequestService.approveMaterialRequest(id, approverId);
    }

    @Override
    public void rejectMaterialRequest(Long id, String rejectReason, Long approverId) {
        materialRequestService.rejectMaterialRequest(id, rejectReason, approverId);
    }

    @Override
    public void receiveMaterialRequest(Long id, Long receiverId) {
        materialRequestService.receiveMaterialRequest(id, receiverId);
    }
}
