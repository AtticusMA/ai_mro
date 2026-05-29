package com.mro.manual.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.manual.request.CreateVersionCommand;
import com.mro.common.dubbo.manual.response.ManualVersionDTO;
import com.mro.manual.domain.entity.ManualDocument;
import com.mro.manual.domain.entity.ManualVersion;
import com.mro.manual.mapper.ManualDocumentMapper;
import com.mro.manual.mapper.ManualVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualVersionService {

    private static final int ERR_DOC_NOT_FOUND = 4500;
    private static final int ERR_NOT_PARSED = 4502;

    private final ManualVersionMapper versionMapper;
    private final ManualDocumentMapper documentMapper;

    public PageResult<ManualVersionDTO> listVersions(Long documentId, int pageNum, int pageSize) {
        Page<ManualVersion> page = versionMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ManualVersion>()
                        .eq(ManualVersion::getDocumentId, documentId)
                        .orderByDesc(ManualVersion::getCreateTime)
        );
        List<ManualVersionDTO> dtos = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(dtos, page.getTotal(), pageNum, pageSize);
    }

    @Transactional
    public Long createVersion(Long documentId, CreateVersionCommand cmd) {
        ManualDocument doc = documentMapper.selectById(documentId);
        if (doc == null) throw new BizException(ERR_DOC_NOT_FOUND, "文档不存在");
        if (!"DONE".equals(doc.getParsedStatus())) throw new BizException(ERR_NOT_PARSED, "文档未解析，不可建版本");

        ManualVersion version = new ManualVersion();
        version.setDocumentId(documentId);
        version.setVersionNo(cmd.versionNo());
        version.setChangeSummary(cmd.changeSummary());
        version.setEffectiveDate(cmd.effectiveDate());
        version.setRevisedBy(cmd.revisedBy());
        versionMapper.insert(version);
        return version.getId();
    }

    private ManualVersionDTO toDTO(ManualVersion v) {
        return new ManualVersionDTO(
                v.getId(),
                v.getDocumentId(),
                v.getVersionNo(),
                v.getChangeSummary(),
                v.getEffectiveDate(),
                v.getRevisedByName(),
                v.getCreateTime() != null ? v.getCreateTime().toInstant(java.time.ZoneOffset.UTC) : null
        );
    }
}
