package com.mro.tool.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.material.request.*;
import com.mro.common.dubbo.material.response.*;
import com.mro.common.dubbo.material.service.MaterialDubboService;
import com.mro.tool.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 航材库存 / 补货预警 / 送修工单 Dubbo 实现
 * Refs: MRO-006
 */
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class MaterialDubboServiceImpl implements MaterialDubboService {

    private final MaterialService materialService;

    @Override
    public PageResult<MaterialItemDTO> listMaterials(MaterialQueryParam param) {
        return materialService.listMaterials(param);
    }

    @Override
    public Long createMaterial(CreateMaterialCommand cmd) {
        return materialService.createMaterial(cmd);
    }

    @Override
    public void updateMaterial(UpdateMaterialCommand cmd) {
        materialService.updateMaterial(cmd);
    }

    @Override
    public PageResult<MaterialAlertDTO> listRestockAlerts(int pageNum, int pageSize) {
        return materialService.listRestockAlerts(pageNum, pageSize);
    }

    @Override
    public Long createRepairOrder(CreateRepairOrderCommand cmd) {
        return materialService.createRepairOrder(cmd);
    }

    @Override
    public PageResult<RepairOrderDTO> listRepairOrders(int pageNum, int pageSize) {
        return materialService.listRepairOrders(pageNum, pageSize);
    }
}
