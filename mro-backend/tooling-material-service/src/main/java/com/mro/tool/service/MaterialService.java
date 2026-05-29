package com.mro.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.tool.request.*;
import com.mro.common.dubbo.material.request.*;
import com.mro.common.dubbo.material.response.*;
import com.mro.common.dubbo.tool.response.*;
import com.mro.tool.domain.entity.MaterialItem;
import com.mro.tool.domain.entity.RepairOrder;
import com.mro.tool.mapper.MaterialItemMapper;
import com.mro.tool.mapper.RepairOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private static final int ERR_MATERIAL_NOT_FOUND = 4706;
    private static final int ERR_PART_NO_EXISTS     = 4707;
    private static final int ERR_REPAIR_NOT_FOUND   = 4708;

    private final MaterialItemMapper materialMapper;
    private final RepairOrderMapper repairOrderMapper;

    public PageResult<MaterialItemDTO> listMaterials(MaterialQueryParam param) {
        LambdaQueryWrapper<MaterialItem> wrapper = new LambdaQueryWrapper<>();
        if (param.category() != null) wrapper.eq(MaterialItem::getCategory, param.category());
        if (Boolean.TRUE.equals(param.lowStock())) {
            wrapper.apply("stock_qty < min_stock");
        }
        Page<MaterialItem> page = materialMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<MaterialItemDTO> dtos = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    @Transactional
    public Long createMaterial(CreateMaterialCommand cmd) {
        long exists = materialMapper.selectCount(
                new LambdaQueryWrapper<MaterialItem>().eq(MaterialItem::getPartNo, cmd.partNo()));
        if (exists > 0) throw new BizException(ERR_PART_NO_EXISTS, "件号已存在（唯一约束冲突）");
        MaterialItem item = new MaterialItem();
        item.setPartNo(cmd.partNo());
        item.setName(cmd.name());
        item.setCategory(cmd.category());
        item.setStockQty(cmd.stockQty());
        item.setMinStock(cmd.minStock());
        item.setLocation(cmd.location());
        item.setExpiryDate(cmd.expiryDate());
        item.setUnitPrice(cmd.unitPrice());
        materialMapper.insert(item);
        return item.getId();
    }

    @Transactional
    public void updateMaterial(UpdateMaterialCommand cmd) {
        MaterialItem item = materialMapper.selectById(cmd.id());
        if (item == null) throw new BizException(ERR_MATERIAL_NOT_FOUND, "航材不存在");
        item.setPartNo(cmd.partNo());
        item.setName(cmd.name());
        item.setCategory(cmd.category());
        item.setStockQty(cmd.stockQty());
        item.setMinStock(cmd.minStock());
        item.setLocation(cmd.location());
        item.setExpiryDate(cmd.expiryDate());
        item.setUnitPrice(cmd.unitPrice());
        materialMapper.updateById(item);
    }

    public PageResult<MaterialAlertDTO> listRestockAlerts(int pageNum, int pageSize) {
        Page<MaterialItem> page = materialMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MaterialItem>().apply("stock_qty < min_stock"));
        List<MaterialAlertDTO> dtos = page.getRecords().stream()
                .map(m -> new MaterialAlertDTO(m.getId(), m.getPartNo(), m.getName(),
                        m.getStockQty(), m.getMinStock(), m.getLocation()))
                .toList();
        return PageResult.of(dtos, page.getTotal(), pageNum, pageSize);
    }

    @Transactional
    public Long createRepairOrder(CreateRepairOrderCommand cmd) {
        MaterialItem item = materialMapper.selectById(cmd.materialId());
        if (item == null) throw new BizException(ERR_MATERIAL_NOT_FOUND, "航材不存在");
        RepairOrder order = new RepairOrder();
        order.setMaterialId(cmd.materialId());
        order.setQuantity(cmd.quantity());
        order.setFaultDescription(cmd.faultDescription());
        order.setVendorId(cmd.vendorId());
        order.setStatus("pending");
        order.setCreatedBy(cmd.createdBy());
        order.setCreatedAt(Instant.now());
        repairOrderMapper.insert(order);
        return order.getId();
    }

    public PageResult<RepairOrderDTO> listRepairOrders(int pageNum, int pageSize) {
        Page<RepairOrder> page = repairOrderMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<RepairOrder>().orderByDesc(RepairOrder::getCreatedAt));
        List<RepairOrderDTO> dtos = page.getRecords().stream().map(r -> {
            MaterialItem item = materialMapper.selectById(r.getMaterialId());
            String materialName = item != null ? item.getName() : null;
            String partNo = item != null ? item.getPartNo() : null;
            return new RepairOrderDTO(r.getId(), r.getMaterialId(), materialName, partNo,
                    r.getQuantity(), r.getFaultDescription(), r.getVendorId(),
                    r.getStatus(), r.getCreatedAt());
        }).toList();
        return PageResult.of(dtos, page.getTotal(), pageNum, pageSize);
    }

    private MaterialItemDTO toDTO(MaterialItem m) {
        boolean below = m.getStockQty() != null && m.getMinStock() != null
                && m.getStockQty() < m.getMinStock();
        return new MaterialItemDTO(m.getId(), m.getPartNo(), m.getName(), m.getCategory(),
                m.getStockQty() != null ? m.getStockQty() : 0,
                m.getMinStock() != null ? m.getMinStock() : 0,
                m.getLocation(), m.getExpiryDate(), m.getUnitPrice(), below);
    }
}
