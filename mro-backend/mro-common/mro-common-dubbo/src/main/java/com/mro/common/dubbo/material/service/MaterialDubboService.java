package com.mro.common.dubbo.material.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.material.request.*;
import com.mro.common.dubbo.material.response.*;

/**
 * 航材库存 / 补货预警 / 送修工单 Dubbo 接口
 * Refs: MRO-006
 */
public interface MaterialDubboService {

    PageResult<MaterialItemDTO> listMaterials(MaterialQueryParam param);

    Long createMaterial(CreateMaterialCommand cmd);

    void updateMaterial(UpdateMaterialCommand cmd);

    PageResult<MaterialAlertDTO> listRestockAlerts(int pageNum, int pageSize);

    Long createRepairOrder(CreateRepairOrderCommand cmd);

    PageResult<RepairOrderDTO> listRepairOrders(int pageNum, int pageSize);
}
