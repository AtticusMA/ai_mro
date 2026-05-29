package com.mro.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.material.request.*;
import com.mro.common.dubbo.material.response.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.tool.context.DataScopeContext;
import com.mro.tool.domain.entity.MaterialItem;
import com.mro.tool.domain.entity.MaterialRequest;
import com.mro.tool.mapper.MaterialItemMapper;
import com.mro.tool.mapper.MaterialRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MaterialRequestService {

    private static final int ERR_REQUEST_NOT_FOUND  = 4710;
    private static final int ERR_WRONG_STATUS        = 4711;
    private static final int ERR_INSUFFICIENT_STOCK  = 4712;
    private static final int ERR_DUPLICATE_REQUEST_NO = 4713;

    private static final String STATUS_PENDING_APPROVAL = "pending_approval";
    private static final String STATUS_APPROVED          = "approved";
    private static final String STATUS_REJECTED          = "rejected";
    private static final String STATUS_RECEIVED          = "received";

    private final MaterialRequestMapper materialRequestMapper;
    private final MaterialItemMapper    materialItemMapper;
    private final ObjectMapper          objectMapper;

    // ------------------------------------------------------------------ create

    public Long createMaterialRequest(CreateMaterialRequestCommand cmd) {
        String requestNo = String.format("MR-%s-%04d",
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                ThreadLocalRandom.current().nextInt(1000, 9999));

        String itemsJson;
        try {
            itemsJson = objectMapper.writeValueAsString(cmd.items());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize items", e);
        }

        MaterialRequest entity = new MaterialRequest();
        entity.setRequestNo(requestNo);
        entity.setWorkcardId(cmd.workcardId());
        entity.setRequesterId(cmd.requesterId());
        entity.setDeptId(cmd.deptId());
        entity.setUrgency(cmd.urgency());
        entity.setItemsJson(itemsJson);
        entity.setStatus(STATUS_PENDING_APPROVAL);
        entity.setCreatedBy(cmd.requesterId());

        materialRequestMapper.insert(entity);
        return entity.getId();
    }

    // ----------------------------------------------------------------- approve

    public void approveMaterialRequest(Long id, Long approverId) {
        MaterialRequest entity = loadOrThrow(id);
        if (!STATUS_PENDING_APPROVAL.equals(entity.getStatus())) {
            throw new BizException(ERR_WRONG_STATUS, "状态不允许操作");
        }
        entity.setStatus(STATUS_APPROVED);
        entity.setApprovedBy(approverId);
        entity.setApprovedAt(LocalDateTime.now());
        materialRequestMapper.updateById(entity);
    }

    // ------------------------------------------------------------------ reject

    public void rejectMaterialRequest(Long id, String rejectReason, Long approverId) {
        MaterialRequest entity = loadOrThrow(id);
        if (!STATUS_PENDING_APPROVAL.equals(entity.getStatus())) {
            throw new BizException(ERR_WRONG_STATUS, "状态不允许操作");
        }
        entity.setStatus(STATUS_REJECTED);
        entity.setRejectReason(rejectReason);
        entity.setApprovedBy(approverId);
        entity.setApprovedAt(LocalDateTime.now());
        materialRequestMapper.updateById(entity);
    }

    // ----------------------------------------------------------------- receive

    @Transactional
    public void receiveMaterialRequest(Long id, Long receiverId) {
        MaterialRequest entity = loadOrThrow(id);
        if (!STATUS_APPROVED.equals(entity.getStatus())) {
            throw new BizException(ERR_WRONG_STATUS, "状态不允许操作");
        }

        List<MaterialRequestItemDTO> items = deserializeItems(entity.getItemsJson());
        for (MaterialRequestItemDTO item : items) {
            int affected = materialItemMapper.update(null, new LambdaUpdateWrapper<MaterialItem>()
                    .setSql("stock_qty = stock_qty - " + item.qty())
                    .eq(MaterialItem::getPartNo, item.partNo())
                    .ge(MaterialItem::getStockQty, item.qty()));
            if (affected == 0) {
                throw new BizException(ERR_INSUFFICIENT_STOCK, "航材库存不足: " + item.partNo());
            }
        }

        entity.setStatus(STATUS_RECEIVED);
        entity.setReceivedBy(receiverId);
        entity.setReceivedAt(LocalDateTime.now());
        materialRequestMapper.updateById(entity);
    }

    // ------------------------------------------------------------------- list

    public PageResult<MaterialRequestDTO> listMaterialRequests(MaterialRequestQueryParam param) {
        LambdaQueryWrapper<MaterialRequest> wrapper = new LambdaQueryWrapper<>();
        if (param.workcardId() != null) wrapper.eq(MaterialRequest::getWorkcardId, param.workcardId());
        if (param.status()     != null) wrapper.eq(MaterialRequest::getStatus,     param.status());
        if (param.requesterId() != null) wrapper.eq(MaterialRequest::getRequesterId, param.requesterId());
        wrapper.orderByDesc(MaterialRequest::getCreateTime);

        // 数据权限过滤
        if (DataScopeContext.isSelfOnly()) {
            Long userId = DataScopeContext.getUserId();
            if (userId != null) {
                wrapper.eq(MaterialRequest::getRequesterId, userId);
            }
        } else {
            java.util.List<Long> deptIds = DataScopeContext.getDeptIds();
            if (deptIds != null) {
                if (deptIds.isEmpty()) {
                    return PageResult.of(java.util.Collections.emptyList(), 0L, param.pageNum(), param.pageSize());
                }
                wrapper.in(MaterialRequest::getDeptId, deptIds);
            }
        }

        Page<MaterialRequest> page = materialRequestMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);

        List<MaterialRequestDTO> dtos = page.getRecords().stream()
                .map(r -> new MaterialRequestDTO(
                        r.getId(),
                        r.getRequestNo(),
                        r.getWorkcardId(),
                        r.getRequesterId(),
                        r.getUrgency(),
                        r.getStatus(),
                        countItems(r.getItemsJson()),
                        r.getCreateTime()))
                .toList();

        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    // -------------------------------------------------------------------- get

    public MaterialRequestDetailDTO getMaterialRequest(Long id) {
        MaterialRequest entity = loadOrThrow(id);
        List<MaterialRequestItemDTO> items = deserializeItems(entity.getItemsJson());
        return new MaterialRequestDetailDTO(
                entity.getId(),
                entity.getRequestNo(),
                entity.getWorkcardId(),
                entity.getRequesterId(),
                entity.getDeptId(),
                entity.getUrgency(),
                entity.getStatus(),
                entity.getRejectReason(),
                items,
                entity.getApprovedBy(),
                entity.getApprovedAt(),
                entity.getReceivedBy(),
                entity.getReceivedAt(),
                entity.getCreateTime());
    }

    // ---------------------------------------------------------------- bom

    public WorkcardBomDTO getWorkcardBom(Long workcardId) {
        MaterialRequest latest = materialRequestMapper.selectOne(
                new LambdaQueryWrapper<MaterialRequest>()
                        .eq(MaterialRequest::getWorkcardId, workcardId)
                        .orderByDesc(MaterialRequest::getCreateTime)
                        .last("LIMIT 1"));
        if (latest == null) {
            return new WorkcardBomDTO(Collections.emptyList());
        }
        return new WorkcardBomDTO(deserializeItems(latest.getItemsJson()));
    }

    // ---------------------------------------------------------------- helpers

    private MaterialRequest loadOrThrow(Long id) {
        MaterialRequest entity = materialRequestMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ERR_REQUEST_NOT_FOUND, "申请单不存在");
        }
        return entity;
    }

    private List<MaterialRequestItemDTO> deserializeItems(String itemsJson) {
        if (itemsJson == null || itemsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(itemsJson,
                    new TypeReference<List<MaterialRequestItemDTO>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize items", e);
        }
    }

    private int countItems(String itemsJson) {
        if (itemsJson == null || itemsJson.isBlank()) return 0;
        try {
            List<?> list = objectMapper.readValue(itemsJson, new TypeReference<List<?>>() {});
            return list.size();
        } catch (JsonProcessingException e) {
            return 0;
        }
    }
}
