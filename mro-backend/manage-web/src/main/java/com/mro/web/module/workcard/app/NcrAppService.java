package com.mro.web.module.workcard.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.workcard.request.CloseNcrCommand;
import com.mro.common.dubbo.workcard.request.CreateNcrCommand;
import com.mro.common.dubbo.workcard.request.NcrQueryParam;
import com.mro.common.dubbo.workcard.response.NcrDTO;
import com.mro.common.dubbo.workcard.service.NcrDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * NCR 应用服务
 * Refs: MRO-008
 */
@Service
@RequiredArgsConstructor
public class NcrAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private NcrDubboService ncrDubboService;

    public PageResult<NcrDTO> listNcrs(Long workcardId, String status, int pageNum, int pageSize) {
        return ncrDubboService.listNcrs(new NcrQueryParam(workcardId, status, pageNum, pageSize));
    }

    public NcrDTO getNcr(Long id) {
        return ncrDubboService.getNcr(id);
    }

    public Long createNcr(CreateNcrCommand cmd) {
        return ncrDubboService.createNcr(cmd, UserContext.getUserId());
    }

    public void closeNcr(Long ncrId, String closeSignature) {
        ncrDubboService.closeNcr(new CloseNcrCommand(ncrId, closeSignature), UserContext.getUserId());
    }
}
