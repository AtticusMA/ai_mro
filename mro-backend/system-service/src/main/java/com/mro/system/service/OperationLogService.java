package com.mro.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.response.OperationLogDTO;
import com.mro.common.dubbo.system.request.OperationLogQueryParam;
import com.mro.common.dubbo.system.request.SaveOperationLogCommand;
import com.mro.system.entity.SysOperationLog;
import com.mro.system.mapper.SysOperationLogMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogService {

    @Autowired
    private SysOperationLogMapper operationLogMapper;

    public void save(SaveOperationLogCommand cmd) {
        SysOperationLog entity = new SysOperationLog();
        BeanUtils.copyProperties(cmd, entity);
        operationLogMapper.insert(entity);
    }

    public PageResult<OperationLogDTO> listPage(OperationLogQueryParam param) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .like(StringUtils.hasText(param.getOperatorName()),
                        SysOperationLog::getOperatorName, param.getOperatorName())
                .like(StringUtils.hasText(param.getRequestUri()),
                        SysOperationLog::getRequestUri, param.getRequestUri())
                .eq(param.getDeptId() != null,
                        SysOperationLog::getDeptId, param.getDeptId())
                .ge(param.getStartTime() != null,
                        SysOperationLog::getRequestTime, param.getStartTime())
                .le(param.getEndTime() != null,
                        SysOperationLog::getRequestTime, param.getEndTime())
                .orderByDesc(SysOperationLog::getRequestTime);

        Page<SysOperationLog> page = operationLogMapper.selectPage(
                new Page<>(param.getPageNum(), param.getPageSize()), wrapper);

        List<OperationLogDTO> records = page.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal(), param.getPageNum(), param.getPageSize());
    }

    public OperationLogDTO getById(Long id) {
        SysOperationLog entity = operationLogMapper.selectById(id);
        return entity != null ? toDTO(entity) : null;
    }

    private OperationLogDTO toDTO(SysOperationLog entity) {
        OperationLogDTO dto = new OperationLogDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
