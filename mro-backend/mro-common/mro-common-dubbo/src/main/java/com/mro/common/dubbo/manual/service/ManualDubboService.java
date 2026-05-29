package com.mro.common.dubbo.manual.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.manual.request.*;
import com.mro.common.dubbo.manual.response.*;

public interface ManualDubboService {

    PageResult<ManualDocDTO> listManuals(ManualQueryParam param, UserContextDTO ctx);

    ManualDocDTO getManual(Long id, UserContextDTO ctx);

    Long createManual(CreateManualCommand cmd);

    void deleteManual(Long id, Long operatorId);

    void triggerParse(Long id, Long operatorId);

    void publishManual(Long id, Long operatorId);

    PageResult<ManualVersionDTO> listVersions(Long documentId, int pageNum, int pageSize);

    Long createVersion(Long documentId, CreateVersionCommand cmd);

    Long submitTranslation(Long documentId, String sourceLang, String targetLang, Long operatorId);

    TranslationTaskDTO getTranslationResult(Long taskId);

    PageResult<ManualSearchResultDTO> searchManuals(ManualSearchParam param, UserContextDTO ctx);
}
