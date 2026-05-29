package com.mro.common.dubbo.workcard.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.workcard.request.CloseNcrCommand;
import com.mro.common.dubbo.workcard.request.CreateNcrCommand;
import com.mro.common.dubbo.workcard.request.NcrQueryParam;
import com.mro.common.dubbo.workcard.response.NcrDTO;

/**
 * NCR (Non-Conformance Report) Dubbo 接口
 * Refs: MRO-008
 */
public interface NcrDubboService {

    Long createNcr(CreateNcrCommand cmd, Long createdBy);

    void closeNcr(CloseNcrCommand cmd, Long closerId);

    PageResult<NcrDTO> listNcrs(NcrQueryParam param);

    NcrDTO getNcr(Long id);
}
