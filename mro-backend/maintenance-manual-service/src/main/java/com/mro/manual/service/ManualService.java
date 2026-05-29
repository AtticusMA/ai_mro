package com.mro.manual.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.manual.request.CreateManualCommand;
import com.mro.common.dubbo.manual.response.ManualDocDTO;
import com.mro.common.dubbo.manual.request.ManualQueryParam;
import com.mro.manual.domain.entity.ManualDocument;
import com.mro.manual.mapper.ManualDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualService {

    private static final int ERR_DOC_NOT_FOUND  = 4500;
    private static final int ERR_NO_DUP         = 4501;
    private static final int ERR_PUBLISHED      = 4504;

    private final ManualDocumentMapper documentMapper;
    private final DocumentParserService parserService;

    public PageResult<ManualDocDTO> listManuals(ManualQueryParam param) {
        LambdaQueryWrapper<ManualDocument> wrapper = new LambdaQueryWrapper<ManualDocument>()
                .eq(StringUtils.hasText(param.parsedStatus()), ManualDocument::getParsedStatus, param.parsedStatus())
                .eq(StringUtils.hasText(param.aircraftType()), ManualDocument::getAircraftType, param.aircraftType())
                .like(StringUtils.hasText(param.manualNo()), ManualDocument::getManualNo, param.manualNo())
                .orderByDesc(ManualDocument::getCreateTime);

        Page<ManualDocument> page = documentMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);

        List<ManualDocDTO> dtos = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    public ManualDocDTO getManual(Long id) {
        ManualDocument doc = documentMapper.selectById(id);
        if (doc == null) throw new BizException(ERR_DOC_NOT_FOUND, "文档不存在");
        return toDTO(doc);
    }

    @Transactional
    public Long createManual(CreateManualCommand cmd) {
        Long existing = documentMapper.selectCount(
                new LambdaQueryWrapper<ManualDocument>().eq(ManualDocument::getManualNo, cmd.manualNo()));
        if (existing > 0) throw new BizException(ERR_NO_DUP, "手册编号已存在");

        ManualDocument doc = new ManualDocument();
        doc.setTitle(cmd.title());
        doc.setManualNo(cmd.manualNo());
        doc.setAircraftType(cmd.aircraftType());
        doc.setFormat(cmd.format());
        doc.setFileUrl(cmd.fileUrl());
        doc.setUploaderId(cmd.uploaderId());
        doc.setParsedStatus("PENDING");
        doc.setPublished(false);
        documentMapper.insert(doc);
        return doc.getId();
    }

    @Transactional
    public void deleteManual(Long id, Long operatorId) {
        ManualDocument doc = documentMapper.selectById(id);
        if (doc == null) throw new BizException(ERR_DOC_NOT_FOUND, "文档不存在");
        if (Boolean.TRUE.equals(doc.getPublished())) throw new BizException(ERR_PUBLISHED, "已发布手册不可删除");
        documentMapper.deleteById(id);
    }

    public void triggerParse(Long id, Long operatorId) {
        ManualDocument doc = documentMapper.selectById(id);
        if (doc == null) throw new BizException(ERR_DOC_NOT_FOUND, "文档不存在");
        parserService.parseAsync(id);
    }

    @Transactional
    public void publishManual(Long id, Long operatorId) {
        ManualDocument doc = documentMapper.selectById(id);
        if (doc == null) throw new BizException(ERR_DOC_NOT_FOUND, "文档不存在");

        ManualDocument update = new ManualDocument();
        update.setId(id);
        update.setPublished(true);
        documentMapper.updateById(update);
    }

    private ManualDocDTO toDTO(ManualDocument doc) {
        return new ManualDocDTO(
                doc.getId(),
                doc.getTitle(),
                doc.getManualNo(),
                doc.getAircraftType(),
                doc.getFormat(),
                doc.getParsedStatus(),
                doc.getCreateTime() != null
                        ? doc.getCreateTime().toInstant(java.time.ZoneOffset.UTC)
                        : null
        );
    }
}
