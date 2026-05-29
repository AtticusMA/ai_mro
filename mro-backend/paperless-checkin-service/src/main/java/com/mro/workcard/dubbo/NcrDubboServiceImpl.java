package com.mro.workcard.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.workcard.request.CloseNcrCommand;
import com.mro.common.dubbo.workcard.request.CreateNcrCommand;
import com.mro.common.dubbo.workcard.request.NcrQueryParam;
import com.mro.common.dubbo.workcard.response.NcrDTO;
import com.mro.common.dubbo.workcard.service.NcrDubboService;
import com.mro.workcard.service.NcrService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * NCR Dubbo 实现
 * Refs: MRO-008
 */
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class NcrDubboServiceImpl implements NcrDubboService {

    private final NcrService ncrService;

    @Override
    public Long createNcr(CreateNcrCommand cmd, Long createdBy) {
        return ncrService.createNcr(cmd, createdBy);
    }

    @Override
    public void closeNcr(CloseNcrCommand cmd, Long closerId) {
        ncrService.closeNcr(cmd, closerId);
    }

    @Override
    public PageResult<NcrDTO> listNcrs(NcrQueryParam param) {
        return ncrService.listNcrs(param);
    }

    @Override
    public NcrDTO getNcr(Long id) {
        return ncrService.getNcr(id);
    }
}
