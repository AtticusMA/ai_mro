package com.mro.workcard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.workcard.request.CloseNcrCommand;
import com.mro.common.dubbo.workcard.request.CreateNcrCommand;
import com.mro.common.dubbo.workcard.response.NcrDTO;
import com.mro.common.dubbo.workcard.request.NcrQueryParam;
import com.mro.workcard.domain.entity.Ncr;
import com.mro.workcard.mapper.NcrMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NcrService {

    private final NcrMapper ncrMapper;

    public Long createNcr(CreateNcrCommand cmd, Long createdBy) {
        Ncr ncr = new Ncr();
        ncr.setWorkcardId(cmd.workcardId());
        ncr.setQualitySignId(cmd.qualitySignId());
        ncr.setNcrNo(String.format("NCR-%s-%04d",
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                ThreadLocalRandom.current().nextInt(1000, 9999)));
        ncr.setTitle(cmd.title());
        ncr.setDescription(cmd.description());
        ncr.setSeverity(cmd.severity());
        ncr.setAssignedTo(cmd.assignedTo());
        ncr.setStatus("open");
        ncr.setCreatedBy(createdBy);
        ncrMapper.insert(ncr);
        return ncr.getId();
    }

    @Transactional
    public void closeNcr(CloseNcrCommand cmd, Long closerId) {
        Ncr ncr = ncrMapper.selectById(cmd.ncrId());
        if (ncr == null) {
            throw new BizException(4913, "NCR不存在");
        }
        if (cmd.closeSignature() == null || cmd.closeSignature().isBlank()) {
            throw new BizException(4914, "关闭NCR需要提供数字签名");
        }
        if ("closed".equals(ncr.getStatus())) {
            throw new BizException(4915, "NCR已关闭");
        }
        ncrMapper.update(null, new LambdaUpdateWrapper<Ncr>()
                .set(Ncr::getStatus, "closed")
                .set(Ncr::getCloseSignature, cmd.closeSignature())
                .set(Ncr::getClosedAt, LocalDateTime.now())
                .eq(Ncr::getId, cmd.ncrId()));
    }

    public PageResult<NcrDTO> listNcrs(NcrQueryParam param) {
        LambdaQueryWrapper<Ncr> wrapper = new LambdaQueryWrapper<Ncr>()
                .orderByDesc(Ncr::getCreateTime);
        if (param.workcardId() != null) {
            wrapper.eq(Ncr::getWorkcardId, param.workcardId());
        }
        if (param.status() != null && !param.status().isBlank()) {
            wrapper.eq(Ncr::getStatus, param.status());
        }
        Page<Ncr> page = ncrMapper.selectPage(new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<NcrDTO> dtoList = page.getRecords().stream().map(this::toDTO).collect(Collectors.toList());
        return PageResult.of(dtoList, page.getTotal(), param.pageNum(), param.pageSize());
    }

    public NcrDTO getNcr(Long id) {
        Ncr ncr = ncrMapper.selectById(id);
        if (ncr == null) {
            throw new BizException(4913, "NCR不存在");
        }
        return toDTO(ncr);
    }

    private NcrDTO toDTO(Ncr n) {
        return new NcrDTO(n.getId(), n.getWorkcardId(), n.getQualitySignId(),
                n.getNcrNo(), n.getTitle(), n.getDescription(), n.getSeverity(),
                n.getStatus(), n.getAssignedTo(), null, n.getClosedAt(),
                n.getCreatedBy(), n.getCreateTime());
    }
}
