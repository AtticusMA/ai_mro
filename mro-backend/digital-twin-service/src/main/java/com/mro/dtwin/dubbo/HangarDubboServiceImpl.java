package com.mro.dtwin.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.dtwin.request.AnalyticsParam;
import com.mro.common.dubbo.dtwin.request.PageParam;
import com.mro.common.dubbo.dtwin.response.EfficiencyAnalyticsDTO;
import com.mro.common.dubbo.dtwin.response.HangarDTO;
import com.mro.common.dubbo.dtwin.response.HangarModelDTO;
import com.mro.common.dubbo.dtwin.response.WorkloadAnalyticsDTO;
import com.mro.common.dubbo.dtwin.response.WorkstationDTO;
import com.mro.common.dubbo.dtwin.service.HangarDubboService;
import com.mro.dtwin.domain.entity.HangarModel;
import com.mro.dtwin.service.AnalyticsService;
import com.mro.dtwin.service.WorkstationService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 机库 / 工位 / 数据分析 Dubbo 实现
 * Refs: MRO-005
 */
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class HangarDubboServiceImpl implements HangarDubboService {

    private final WorkstationService workstationService;
    private final AnalyticsService analyticsService;

    @Override
    public PageResult<HangarDTO> listHangars(UserContextDTO ctx) {
        List<HangarModel> hangars = workstationService.listHangars();
        List<HangarDTO> dtos = hangars.stream()
                .map(h -> new HangarDTO(h.getId(), h.getName()))
                .toList();
        return PageResult.of(dtos, (long) dtos.size(), 1, dtos.isEmpty() ? 10 : dtos.size());
    }

    @Override
    public HangarModelDTO getHangarModel(Long hangarId) {
        HangarModel h = workstationService.getHangar(hangarId);
        return new HangarModelDTO(h.getId(), h.getName(), h.getModelUrl(), h.getVersion());
    }

    @Override
    public PageResult<WorkstationDTO> listWorkstations(Long hangarId, PageParam param) {
        return workstationService.listWorkstations(hangarId, param.pageNum(), param.pageSize());
    }

    @Override
    public WorkloadAnalyticsDTO analyzeWorkload(AnalyticsParam param) {
        return analyticsService.analyzeWorkload(param);
    }

    @Override
    public EfficiencyAnalyticsDTO analyzeEfficiency(AnalyticsParam param) {
        return analyticsService.analyzeEfficiency(param);
    }
}
