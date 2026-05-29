package com.mro.web.module.tool.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.common.dubbo.material.request.*;
import com.mro.common.dubbo.material.response.*;
import com.mro.common.dubbo.material.service.MaterialDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaterialAppService {

    @DubboReference(version = "1.0.0")
    private MaterialDubboService materialDubboService;

    public PageResult<MaterialItemDTO> listMaterials(String category, Boolean lowStock,
                                                      int pageNum, int pageSize) {
        return materialDubboService.listMaterials(new MaterialQueryParam(pageNum, pageSize, category, lowStock));
    }

    public Long createMaterial(CreateMaterialCommand cmd) {
        Long userId = UserContext.getUserId();
        return materialDubboService.createMaterial(new CreateMaterialCommand(
                cmd.partNo(), cmd.name(), cmd.category(), cmd.stockQty(), cmd.minStock(),
                cmd.location(), cmd.expiryDate(), cmd.unitPrice(), userId));
    }

    public void updateMaterial(UpdateMaterialCommand cmd) {
        Long userId = UserContext.getUserId();
        materialDubboService.updateMaterial(new UpdateMaterialCommand(
                cmd.id(), cmd.partNo(), cmd.name(), cmd.category(), cmd.stockQty(), cmd.minStock(),
                cmd.location(), cmd.expiryDate(), cmd.unitPrice(), userId));
    }

    public PageResult<MaterialAlertDTO> listRestockAlerts(int pageNum, int pageSize) {
        return materialDubboService.listRestockAlerts(pageNum, pageSize);
    }

    public Long createRepairOrder(CreateRepairOrderCommand cmd) {
        Long userId = UserContext.getUserId();
        return materialDubboService.createRepairOrder(new CreateRepairOrderCommand(
                cmd.materialId(), cmd.quantity(), cmd.faultDescription(), cmd.vendorId(), userId));
    }

    public PageResult<RepairOrderDTO> listRepairOrders(int pageNum, int pageSize) {
        return materialDubboService.listRepairOrders(pageNum, pageSize);
    }
}
