package com.mro.manual.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.manual.request.*;
import com.mro.common.dubbo.manual.response.*;
import com.mro.common.dubbo.manual.request.*;
import com.mro.common.dubbo.manual.response.*;
import com.mro.common.dubbo.manual.service.ManualDubboService;
import com.mro.manual.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class ManualDubboServiceImpl implements ManualDubboService {

    private final ManualService manualService;
    private final ManualVersionService versionService;
    private final TranslationService translationService;
    private final ManualSearchService searchService;

    @Override
    public PageResult<ManualDocDTO> listManuals(ManualQueryParam param, UserContextDTO ctx) {
        return manualService.listManuals(param);
    }

    @Override
    public ManualDocDTO getManual(Long id, UserContextDTO ctx) {
        return manualService.getManual(id);
    }

    @Override
    public Long createManual(CreateManualCommand cmd) {
        return manualService.createManual(cmd);
    }

    @Override
    public void deleteManual(Long id, Long operatorId) {
        manualService.deleteManual(id, operatorId);
    }

    @Override
    public void triggerParse(Long id, Long operatorId) {
        manualService.triggerParse(id, operatorId);
    }

    @Override
    public void publishManual(Long id, Long operatorId) {
        manualService.publishManual(id, operatorId);
    }

    @Override
    public PageResult<ManualVersionDTO> listVersions(Long documentId, int pageNum, int pageSize) {
        return versionService.listVersions(documentId, pageNum, pageSize);
    }

    @Override
    public Long createVersion(Long documentId, CreateVersionCommand cmd) {
        return versionService.createVersion(documentId, cmd);
    }

    @Override
    public Long submitTranslation(Long documentId, String sourceLang, String targetLang, Long operatorId) {
        return translationService.submit(documentId, sourceLang, targetLang, operatorId);
    }

    @Override
    public TranslationTaskDTO getTranslationResult(Long taskId) {
        return translationService.getResult(taskId);
    }

    @Override
    public PageResult<ManualSearchResultDTO> searchManuals(ManualSearchParam param, UserContextDTO ctx) {
        return searchService.search(param);
    }
}
