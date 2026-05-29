package com.mro.training.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.training.request.*;
import com.mro.common.dubbo.training.response.*;
import com.mro.training.domain.entity.TrainingScenario;
import com.mro.training.mapper.TrainingScenarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingScenarioService {

    private static final int ERR_SCENARIO_NOT_FOUND      = 4800;
    private static final int ERR_SCENARIO_NOT_DRAFT      = 4801;

    private final TrainingScenarioMapper scenarioMapper;

    public PageResult<TrainingScenarioDTO> listScenarios(ScenarioQueryParam param) {
        LambdaQueryWrapper<TrainingScenario> wrapper = new LambdaQueryWrapper<>();
        if (param.status() != null) wrapper.eq(TrainingScenario::getStatus, param.status());
        if (param.category() != null) wrapper.eq(TrainingScenario::getCategory, param.category());
        wrapper.orderByDesc(TrainingScenario::getId);
        Page<TrainingScenario> page = scenarioMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<TrainingScenarioDTO> dtos = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    public Long createScenario(CreateScenarioCommand cmd) {
        TrainingScenario entity = new TrainingScenario();
        entity.setName(cmd.name());
        entity.setCategory(cmd.category());
        entity.setDifficulty(cmd.difficulty());
        entity.setModelUrl(cmd.modelUrl());
        entity.setDurationMinutes(cmd.durationMinutes());
        entity.setStatus("draft");
        entity.setCreatedBy(cmd.createdBy());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        scenarioMapper.insert(entity);
        return entity.getId();
    }

    public void updateScenario(UpdateScenarioCommand cmd) {
        TrainingScenario entity = scenarioMapper.selectById(cmd.id());
        if (entity == null) throw new BizException(ERR_SCENARIO_NOT_FOUND, "培训场景不存在");
        entity.setName(cmd.name());
        entity.setCategory(cmd.category());
        entity.setDifficulty(cmd.difficulty());
        entity.setModelUrl(cmd.modelUrl());
        entity.setDurationMinutes(cmd.durationMinutes());
        entity.setUpdatedAt(LocalDateTime.now());
        scenarioMapper.updateById(entity);
    }

    public void publishScenario(Long scenarioId, Long operatorId) {
        TrainingScenario entity = scenarioMapper.selectById(scenarioId);
        if (entity == null) throw new BizException(ERR_SCENARIO_NOT_FOUND, "培训场景不存在");
        if (!"draft".equals(entity.getStatus())) throw new BizException(ERR_SCENARIO_NOT_DRAFT, "只有草稿状态的场景才能发布");
        entity.setStatus("published");
        entity.setUpdatedAt(LocalDateTime.now());
        scenarioMapper.updateById(entity);
    }

    public TrainingScenario getById(Long id) {
        return scenarioMapper.selectById(id);
    }

    private TrainingScenarioDTO toDTO(TrainingScenario s) {
        return new TrainingScenarioDTO(s.getId(), s.getName(), s.getCategory(),
                s.getDifficulty(), s.getModelUrl(),
                s.getDurationMinutes() != null ? s.getDurationMinutes() : 0, s.getStatus());
    }
}
