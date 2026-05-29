package com.mro.common.dubbo.tshoot.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.PageQueryParam;
import com.mro.common.dubbo.tshoot.request.*;
import com.mro.common.dubbo.tshoot.response.*;

public interface TshootDubboService {

    PageResult<KnowledgeBaseDTO> listKnowledgeBases(PageQueryParam param);

    Long createKnowledgeBase(CreateKbCommand cmd);

    Long uploadDocument(Long kbId, UploadDocCommand cmd);

    void deleteDocument(Long kbId, Long docId);

    Long submitQuery(FaultQueryCommand cmd);

    TshootResultDTO getQueryResult(Long queryId);

    PageResult<RepairHistoryDTO> listHistory(HistoryQueryParam param);

    FaultStatisticsDTO getStatistics(StatQueryParam param);

    PageResult<TshootReportDTO> listMyReports(Long userId, PageQueryParam param);

    TshootReportDTO getReport(Long reportId);
}
