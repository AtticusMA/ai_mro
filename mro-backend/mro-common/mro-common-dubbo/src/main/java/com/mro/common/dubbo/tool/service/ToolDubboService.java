package com.mro.common.dubbo.tool.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.tool.response.*;

import java.util.List;

public interface ToolDubboService {

    BorrowResultDTO borrowTools(BorrowCommand cmd);

    ReturnResultDTO returnTools(ReturnCommand cmd);

    PageResult<ToolCabinetDTO> listCabinets(UserContextDTO ctx);

    List<SlotStatusDTO> getCabinetSlots(Long cabinetId);

    void triggerInventory(Long cabinetId);

    PageResult<ToolDTO> listTools(ToolQueryParam param);

    ToolLifecycleDTO getToolLifecycle(Long toolId);

    PageResult<BorrowRecordDTO> listBorrowRecords(BorrowRecordQueryParam param);

    PageResult<ToolAlertDTO> listAlerts(AlertQueryParam param);
}
