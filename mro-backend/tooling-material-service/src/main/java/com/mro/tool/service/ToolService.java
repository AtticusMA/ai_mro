package com.mro.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.tool.domain.entity.BorrowRecord;
import com.mro.tool.domain.entity.Tool;
import com.mro.tool.domain.entity.ToolCabinet;
import com.mro.tool.mapper.BorrowRecordMapper;
import com.mro.tool.mapper.ToolCabinetMapper;
import com.mro.tool.mapper.ToolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolService {

    private static final int ERR_TOOL_NOT_FOUND        = 4700;
    private static final int ERR_TOOL_ALREADY_BORROWED = 4701;
    private static final int ERR_TOOL_NOT_IN_CABINET   = 4702;
    private static final int ERR_CABINET_NOT_FOUND     = 4703;
    private static final int ERR_CABINET_OFFLINE       = 4704;
    private static final int ERR_CALIBRATION_EXPIRED   = 4705;

    private final ToolMapper toolMapper;
    private final ToolCabinetMapper cabinetMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    @Transactional
    public BorrowResultDTO borrowTools(BorrowCommand cmd) {
        ToolCabinet cabinet = cabinetMapper.selectById(cmd.cabinetId());
        if (cabinet == null) throw new BizException(ERR_CABINET_NOT_FOUND, "工具柜不存在");
        if ("offline".equals(cabinet.getOnlineStatus())) throw new BizException(ERR_CABINET_OFFLINE, "工具柜离线，操作失败");

        List<Long> recordIds = new ArrayList<>();
        Instant now = Instant.now();
        Instant expectedReturn = now.plusSeconds((long) cmd.expectedReturnHours() * 3600);

        for (Long toolId : cmd.toolIds()) {
            Tool tool = toolMapper.selectById(toolId);
            if (tool == null) throw new BizException(ERR_TOOL_NOT_FOUND, "工具不存在");
            if (!"in_cabinet".equals(tool.getStatus())) throw new BizException(ERR_TOOL_NOT_IN_CABINET, "工具不在柜内，无法借出");
            if (tool.getCalibrationDue() != null && tool.getCalibrationDue().isBefore(java.time.LocalDate.now()))
                throw new BizException(ERR_CALIBRATION_EXPIRED, "工具检定已过期，不允许借出");

            tool.setStatus("borrowed");
            toolMapper.updateById(tool);
            tool.setUseCount(tool.getUseCount() + 1);
            toolMapper.updateById(tool);

            BorrowRecord record = new BorrowRecord();
            record.setToolId(toolId);
            record.setUserId(cmd.userId());
            record.setBorrowTime(now);
            record.setExpectedReturn(expectedReturn);
            record.setStatus("borrowed");
            record.setWorkcardId(cmd.workcardId());
            borrowRecordMapper.insert(record);
            recordIds.add(record.getId());
        }
        return new BorrowResultDTO(recordIds);
    }

    @Transactional
    public ReturnResultDTO returnTools(ReturnCommand cmd) {
        ToolCabinet cabinet = cabinetMapper.selectById(cmd.cabinetId());
        if (cabinet == null) throw new BizException(ERR_CABINET_NOT_FOUND, "工具柜不存在");

        List<String> missingRfids = new ArrayList<>();
        int returnedCount = 0;

        for (String rfid : cmd.rfidScanResult()) {
            Tool tool = toolMapper.selectOne(
                    new LambdaQueryWrapper<Tool>().eq(Tool::getRfidTag, rfid));
            if (tool == null) {
                missingRfids.add(rfid);
                continue;
            }
            tool.setStatus("in_cabinet");
            toolMapper.updateById(tool);

            BorrowRecord record = borrowRecordMapper.selectOne(
                    new LambdaQueryWrapper<BorrowRecord>()
                            .eq(BorrowRecord::getToolId, tool.getId())
                            .eq(BorrowRecord::getStatus, "borrowed")
                            .orderByDesc(BorrowRecord::getBorrowTime)
                            .last("LIMIT 1"));
            if (record != null) {
                record.setActualReturn(Instant.now());
                record.setStatus("returned");
                borrowRecordMapper.updateById(record);
            }
            returnedCount++;
        }
        return new ReturnResultDTO(returnedCount, missingRfids);
    }

    public PageResult<ToolCabinetDTO> listCabinets(int pageNum, int pageSize) {
        Page<ToolCabinet> page = cabinetMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ToolCabinet>().orderByAsc(ToolCabinet::getId));
        List<ToolCabinetDTO> dtos = page.getRecords().stream().map(this::toCabinetDTO).toList();
        return PageResult.of(dtos, page.getTotal(), pageNum, pageSize);
    }

    public List<SlotStatusDTO> getCabinetSlots(Long cabinetId) {
        List<Tool> tools = cabinetMapper.selectSlotsByCabinet(cabinetId);
        return tools.stream().map(t -> new SlotStatusDTO(
                t.getSlotNo(), t.getId(), t.getName(), t.getToolCode(), t.getRfidTag(), t.getStatus()
        )).toList();
    }

    public void triggerInventory(Long cabinetId) {
        // RFID inventory is hardware-driven; this endpoint acknowledges the trigger
    }

    public PageResult<ToolDTO> listTools(ToolQueryParam param) {
        LambdaQueryWrapper<Tool> wrapper = new LambdaQueryWrapper<>();
        if (param.status() != null) wrapper.eq(Tool::getStatus, param.status());
        if (param.category() != null) wrapper.eq(Tool::getCategory, param.category());
        if (param.cabinetId() != null) wrapper.eq(Tool::getCabinetId, param.cabinetId());
        Page<Tool> page = toolMapper.selectPage(new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<ToolDTO> dtos = page.getRecords().stream().map(t -> {
            ToolCabinet cab = cabinetMapper.selectById(t.getCabinetId());
            String cabName = cab != null ? cab.getName() : null;
            return new ToolDTO(t.getId(), t.getName(), t.getToolCode(), t.getRfidTag(),
                    t.getCategory(), t.getCabinetId(), cabName, t.getSlotNo(),
                    t.getStatus(), t.getCalibrationDue(), t.getUseCount() != null ? t.getUseCount() : 0);
        }).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    public ToolLifecycleDTO getToolLifecycle(Long toolId) {
        Tool tool = toolMapper.selectById(toolId);
        if (tool == null) throw new BizException(ERR_TOOL_NOT_FOUND, "工具不存在");
        String calibStatus = "normal";
        if (tool.getCalibrationDue() != null) {
            if (tool.getCalibrationDue().isBefore(java.time.LocalDate.now())) calibStatus = "expired";
            else if (tool.getCalibrationDue().isBefore(java.time.LocalDate.now().plusDays(30))) calibStatus = "expiring_soon";
        }
        return new ToolLifecycleDTO(tool.getId(), tool.getName(), tool.getToolCode(),
                tool.getUseCount() != null ? tool.getUseCount() : 0,
                tool.getCalibrationDue(), calibStatus, null);
    }

    public PageResult<BorrowRecordDTO> listBorrowRecords(BorrowRecordQueryParam param) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        if (param.userId() != null) wrapper.eq(BorrowRecord::getUserId, param.userId());
        if (param.status() != null) wrapper.eq(BorrowRecord::getStatus, param.status());
        wrapper.orderByDesc(BorrowRecord::getBorrowTime);
        Page<BorrowRecord> page = borrowRecordMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<BorrowRecordDTO> dtos = page.getRecords().stream().map(r -> {
            Tool tool = toolMapper.selectById(r.getToolId());
            String toolName = tool != null ? tool.getName() : null;
            return new BorrowRecordDTO(r.getId(), r.getToolId(), toolName, r.getUserId(), null,
                    r.getBorrowTime(), r.getExpectedReturn(), r.getActualReturn(),
                    r.getStatus(), r.getWorkcardId());
        }).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    public PageResult<ToolAlertDTO> listAlerts(AlertQueryParam param) {
        Instant now = Instant.now();
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getStatus, "borrowed")
                .lt(BorrowRecord::getExpectedReturn, now)
                .orderByAsc(BorrowRecord::getExpectedReturn);
        if (param.alertType() != null && !"overdue".equals(param.alertType())) {
            return PageResult.of(List.of(), 0L, param.pageNum(), param.pageSize());
        }
        Page<BorrowRecord> page = borrowRecordMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<ToolAlertDTO> dtos = page.getRecords().stream().map(r -> {
            Tool tool = toolMapper.selectById(r.getToolId());
            String toolName = tool != null ? tool.getName() : null;
            double overdueHours = (now.toEpochMilli() - r.getExpectedReturn().toEpochMilli()) / 3_600_000.0;
            return new ToolAlertDTO(r.getId(), r.getToolId(), toolName, "overdue",
                    r.getUserId(), null, r.getBorrowTime(), r.getExpectedReturn(),
                    Math.round(overdueHours * 10) / 10.0);
        }).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    private ToolCabinetDTO toCabinetDTO(ToolCabinet c) {
        long total = toolMapper.selectCount(
                new LambdaQueryWrapper<Tool>().eq(Tool::getCabinetId, c.getId()));
        long borrowed = toolMapper.selectCount(
                new LambdaQueryWrapper<Tool>().eq(Tool::getCabinetId, c.getId()).eq(Tool::getStatus, "borrowed"));
        int available = (int) (total - borrowed);
        return new ToolCabinetDTO(c.getId(), c.getName(), c.getLocation(),
                c.getSlotCount() != null ? c.getSlotCount() : 0, available,
                c.getTemperature(), c.getHumidity(), c.getOnlineStatus());
    }
}
