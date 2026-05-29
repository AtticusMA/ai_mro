package com.mro.web.module.workcard.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.workcard.response.QualificationDTO;
import com.mro.common.dubbo.workcard.request.QualificationMatchParam;
import com.mro.common.dubbo.workcard.request.*;
import com.mro.common.dubbo.workcard.response.*;
import com.mro.common.dubbo.workcard.service.WorkcardDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QualificationAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private WorkcardDubboService workcardDubboService;

    public PageResult<QualificationDTO> listQualifications(int pageNum, int pageSize) {
        return workcardDubboService.listQualifications(pageNum, pageSize);
    }

    public PageResult<QualificationDTO> matchQualifications(
            Long workcardId, String aircraftType, String cardType, int pageNum, int pageSize) {
        return workcardDubboService.matchQualifications(
                new QualificationMatchParam(workcardId, aircraftType, cardType, pageNum, pageSize));
    }
}
