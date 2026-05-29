package com.mro.tool.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.common.dubbo.tool.service.ToolDubboService;
import com.mro.tool.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class ToolDubboServiceImpl implements ToolDubboService {

    private final ToolService toolService;

    @Override
    public BorrowResultDTO borrowTools(BorrowCommand cmd) {
        return toolService.borrowTools(cmd);
    }

    @Override
    public ReturnResultDTO returnTools(ReturnCommand cmd) {
        return toolService.returnTools(cmd);
    }

    @Override
    public PageResult<ToolCabinetDTO> listCabinets(UserContextDTO ctx) {
        return toolService.listCabinets(1, 100);
    }

    @Override
    public List<SlotStatusDTO> getCabinetSlots(Long cabinetId) {
        return toolService.getCabinetSlots(cabinetId);
    }

    @Override
    public void triggerInventory(Long cabinetId) {
        toolService.triggerInventory(cabinetId);
    }

    @Override
    public PageResult<ToolDTO> listTools(ToolQueryParam param) {
        return toolService.listTools(param);
    }

    @Override
    public ToolLifecycleDTO getToolLifecycle(Long toolId) {
        return toolService.getToolLifecycle(toolId);
    }

    @Override
    public PageResult<BorrowRecordDTO> listBorrowRecords(BorrowRecordQueryParam param) {
        return toolService.listBorrowRecords(param);
    }

    @Override
    public PageResult<ToolAlertDTO> listAlerts(AlertQueryParam param) {
        return toolService.listAlerts(param);
    }
}
