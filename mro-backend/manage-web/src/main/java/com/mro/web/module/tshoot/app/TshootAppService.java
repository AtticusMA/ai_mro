package com.mro.web.module.tshoot.app;

import com.mro.common.dubbo.common.request.PageQueryParam;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.tshoot.request.*;
import com.mro.common.dubbo.tshoot.response.*;
import com.mro.common.dubbo.tshoot.service.TshootDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TshootAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private TshootDubboService tshootDubboService;

    public PageResult<KnowledgeBaseDTO> listKnowledgeBases(PageQueryParam param) {
        return tshootDubboService.listKnowledgeBases(param);
    }

    public Long createKnowledgeBase(CreateKbCommand cmd) {
        return tshootDubboService.createKnowledgeBase(cmd);
    }

    public Long uploadDocument(Long kbId, UploadDocCommand cmd) {
        return tshootDubboService.uploadDocument(kbId, cmd);
    }

    public void deleteDocument(Long kbId, Long docId) {
        tshootDubboService.deleteDocument(kbId, docId);
    }

    public Long submitQuery(FaultQueryCommand cmd) {
        return tshootDubboService.submitQuery(cmd);
    }

    public TshootResultDTO getQueryResult(Long queryId) {
        return tshootDubboService.getQueryResult(queryId);
    }

    public PageResult<RepairHistoryDTO> listHistory(HistoryQueryParam param) {
        return tshootDubboService.listHistory(param);
    }

    public FaultStatisticsDTO getStatistics(StatQueryParam param) {
        return tshootDubboService.getStatistics(param);
    }

    public PageResult<TshootReportDTO> listMyReports(Long userId, PageQueryParam param) {
        return tshootDubboService.listMyReports(userId, param);
    }

    public TshootReportDTO getReport(Long reportId) {
        return tshootDubboService.getReport(reportId);
    }
}
