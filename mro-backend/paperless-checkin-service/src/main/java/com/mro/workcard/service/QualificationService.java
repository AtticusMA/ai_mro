package com.mro.workcard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.workcard.response.QualificationDTO;
import com.mro.common.dubbo.workcard.request.QualificationMatchParam;
import com.mro.workcard.domain.entity.PersonnelQualification;
import com.mro.workcard.mapper.PersonnelQualificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QualificationService {

    private final PersonnelQualificationMapper qualMapper;

    public PageResult<QualificationDTO> listQualifications(int pageNum, int pageSize) {
        LambdaQueryWrapper<PersonnelQualification> wrapper = new LambdaQueryWrapper<PersonnelQualification>()
                .ge(PersonnelQualification::getValidTo, LocalDate.now())
                .orderByAsc(PersonnelQualification::getUserId);
        Page<PersonnelQualification> page = qualMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<QualificationDTO> dtos = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(dtos, page.getTotal(), pageNum, pageSize);
    }

    public PageResult<QualificationDTO> matchQualifications(QualificationMatchParam param) {
        int offset = (param.pageNum() - 1) * param.pageSize();
        var entities = qualMapper.selectQualifiedPersonnel(
                param.aircraftType(), offset, param.pageSize()).stream().map(this::toDTO).toList();
        long total = qualMapper.countQualifiedPersonnel(param.aircraftType());
        return PageResult.of(entities, total, param.pageNum(), param.pageSize());
    }

    private QualificationDTO toDTO(PersonnelQualification q) {
        return new QualificationDTO(q.getUserId(), null, q.getQualificationType(),
                q.getLevel(), q.getValidTo());
    }
}
