package com.mro.web.module.tool.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.common.dubbo.tool.service.ToolDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolAppService {

    @DubboReference(version = "1.0.0")
    private ToolDubboService toolDubboService;

    public BorrowResultDTO borrowTools(BorrowCommand cmd) {
        return toolDubboService.borrowTools(cmd);
    }

    public ReturnResultDTO returnTools(ReturnCommand cmd) {
        return toolDubboService.returnTools(cmd);
    }

    public PageResult<ToolCabinetDTO> listCabinets() {
        return toolDubboService.listCabinets(UserContext.get());
    }

    public List<SlotStatusDTO> getCabinetSlots(Long cabinetId) {
        return toolDubboService.getCabinetSlots(cabinetId);
    }

    public void triggerInventory(Long cabinetId) {
        toolDubboService.triggerInventory(cabinetId);
    }

    public PageResult<ToolDTO> listTools(String status, String category, Long cabinetId,
                                          int pageNum, int pageSize) {
        return toolDubboService.listTools(new ToolQueryParam(pageNum, pageSize, status, category, cabinetId));
    }

    public ToolLifecycleDTO getToolLifecycle(Long toolId) {
        return toolDubboService.getToolLifecycle(toolId);
    }

    public PageResult<BorrowRecordDTO> listBorrowRecords(Long userId, String status,
                                                          int pageNum, int pageSize) {
        return toolDubboService.listBorrowRecords(new BorrowRecordQueryParam(pageNum, pageSize, userId, status));
    }

    public PageResult<ToolAlertDTO> listAlerts(String alertType, int pageNum, int pageSize) {
        return toolDubboService.listAlerts(new AlertQueryParam(pageNum, pageSize, alertType));
    }
}
