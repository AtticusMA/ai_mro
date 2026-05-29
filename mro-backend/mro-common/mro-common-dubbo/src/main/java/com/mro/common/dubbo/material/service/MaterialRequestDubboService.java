package com.mro.common.dubbo.material.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.material.request.CreateMaterialRequestCommand;
import com.mro.common.dubbo.material.request.MaterialRequestQueryParam;
import com.mro.common.dubbo.material.response.MaterialRequestDTO;
import com.mro.common.dubbo.material.response.MaterialRequestDetailDTO;
import com.mro.common.dubbo.material.response.WorkcardBomDTO;

/**
 * 航材领料申请 Dubbo 接口
 * Refs: MRO-006
 */
public interface MaterialRequestDubboService {

    WorkcardBomDTO getWorkcardBom(Long workcardId);

    Long createMaterialRequest(CreateMaterialRequestCommand cmd);

    PageResult<MaterialRequestDTO> listMaterialRequests(MaterialRequestQueryParam param);

    MaterialRequestDetailDTO getMaterialRequest(Long id);

    void approveMaterialRequest(Long id, Long approverId);

    void rejectMaterialRequest(Long id, String rejectReason, Long approverId);

    void receiveMaterialRequest(Long id, Long receiverId);
}
