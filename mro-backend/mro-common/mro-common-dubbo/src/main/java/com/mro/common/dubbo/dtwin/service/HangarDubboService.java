package com.mro.common.dubbo.dtwin.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.dtwin.request.AnalyticsParam;
import com.mro.common.dubbo.dtwin.request.PageParam;
import com.mro.common.dubbo.dtwin.response.EfficiencyAnalyticsDTO;
import com.mro.common.dubbo.dtwin.response.HangarDTO;
import com.mro.common.dubbo.dtwin.response.HangarModelDTO;
import com.mro.common.dubbo.dtwin.response.WorkloadAnalyticsDTO;
import com.mro.common.dubbo.dtwin.response.WorkstationDTO;

/**
 * 机库 / 工位 / 数据分析 Dubbo 接口
 * Refs: MRO-005
 */
public interface HangarDubboService {

    PageResult<HangarDTO> listHangars(UserContextDTO ctx);

    HangarModelDTO getHangarModel(Long hangarId);

    PageResult<WorkstationDTO> listWorkstations(Long hangarId, PageParam param);

    WorkloadAnalyticsDTO analyzeWorkload(AnalyticsParam param);

    EfficiencyAnalyticsDTO analyzeEfficiency(AnalyticsParam param);
}
