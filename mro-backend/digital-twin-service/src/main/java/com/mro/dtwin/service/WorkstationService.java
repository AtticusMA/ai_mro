package com.mro.dtwin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.dtwin.request.*;
import com.mro.common.dubbo.dtwin.response.*;
import com.mro.dtwin.domain.entity.HangarModel;
import com.mro.dtwin.domain.entity.Workstation;
import com.mro.dtwin.mapper.HangarModelMapper;
import com.mro.dtwin.mapper.WorkstationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkstationService {

    private static final int ERR_HANGAR_NOT_FOUND      = 4600;
    private static final int ERR_WORKSTATION_NOT_FOUND = 4601;

    private final HangarModelMapper hangarMapper;
    private final WorkstationMapper workstationMapper;
    private final DtwinEventPublisher eventPublisher;

    public List<HangarModel> listHangars() {
        return hangarMapper.selectList(new LambdaQueryWrapper<>());
    }

    public HangarModel getHangar(Long id) {
        HangarModel h = hangarMapper.selectById(id);
        if (h == null) throw new BizException(ERR_HANGAR_NOT_FOUND, "机库不存在");
        return h;
    }

    public PageResult<WorkstationDTO> listWorkstations(Long hangarId, int pageNum, int pageSize) {
        Page<Workstation> page = workstationMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Workstation>().eq(Workstation::getHangarId, hangarId));
        List<WorkstationDTO> dtos = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(dtos, page.getTotal(), pageNum, pageSize);
    }

    @Transactional
    public void updateStatus(Long workstationId, String newStatus, String aircraftId) {
        Workstation ws = workstationMapper.selectById(workstationId);
        if (ws == null) throw new BizException(ERR_WORKSTATION_NOT_FOUND, "工位不存在");
        ws.setStatus(newStatus);
        ws.setCurrentAircraftId(aircraftId);
        workstationMapper.updateById(ws);
        eventPublisher.publishWorkstationChange(ws.getHangarId(), workstationId, newStatus, aircraftId);
    }

    public Workstation getWorkstation(Long id) {
        Workstation ws = workstationMapper.selectById(id);
        if (ws == null) throw new BizException(ERR_WORKSTATION_NOT_FOUND, "工位不存在");
        return ws;
    }

    private WorkstationDTO toDTO(Workstation ws) {
        return new WorkstationDTO(
                ws.getId(), ws.getHangarId(), ws.getName(),
                ws.getPositionX(), ws.getPositionY(), ws.getPositionZ(),
                ws.getStatus(), ws.getCurrentAircraftId());
    }
}
