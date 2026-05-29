package com.mro.web.module.manual.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.manual.request.*;
import com.mro.common.dubbo.manual.response.*;
import com.mro.common.dubbo.manual.service.ManualDubboService;
import com.mro.web.context.UserContext;
import com.mro.web.module.manual.support.ManualFileUploader;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ManualAppService {

    private static final Set<String> ALLOWED_FORMATS = Set.of("PDF", "XML", "SGML");
    private static final int ERR_FORMAT = 4506;

    @DubboReference(version = "1.0.0", timeout = 10000, retries = 0)
    private ManualDubboService manualDubboService;

    private final ManualFileUploader fileUploader;

    public PageResult<ManualDocDTO> listManuals(ManualQueryParam param) {
        return manualDubboService.listManuals(param, buildCtx());
    }

    public ManualDocDTO getManual(Long id) {
        return manualDubboService.getManual(id, buildCtx());
    }

    public Long uploadManual(MultipartFile file, String title, String manualNo,
                             String aircraftType, String format) {
        if (!ALLOWED_FORMATS.contains(format.toUpperCase())) {
            throw new com.mro.common.core.exception.BizException(ERR_FORMAT, "仅支持 PDF/XML/SGML 格式");
        }
        String fileUrl = fileUploader.upload(file, manualNo);
        CreateManualCommand cmd = new CreateManualCommand(
                title, manualNo, aircraftType, format.toUpperCase(),
                fileUrl, UserContext.getUserId());
        return manualDubboService.createManual(cmd);
    }

    public void deleteManual(Long id) {
        manualDubboService.deleteManual(id, UserContext.getUserId());
    }

    public void triggerParse(Long id) {
        manualDubboService.triggerParse(id, UserContext.getUserId());
    }

    public void publishManual(Long id) {
        manualDubboService.publishManual(id, UserContext.getUserId());
    }

    public PageResult<ManualVersionDTO> listVersions(Long documentId, int pageNum, int pageSize) {
        return manualDubboService.listVersions(documentId, pageNum, pageSize);
    }

    public Long createVersion(Long documentId, CreateVersionCommand cmd) {
        return manualDubboService.createVersion(documentId, cmd);
    }

    public Long submitTranslation(Long documentId, String sourceLang, String targetLang) {
        return manualDubboService.submitTranslation(
                documentId, sourceLang, targetLang, UserContext.getUserId());
    }

    public TranslationTaskDTO getTranslationResult(Long taskId) {
        return manualDubboService.getTranslationResult(taskId);
    }

    public PageResult<ManualSearchResultDTO> searchManuals(ManualSearchParam param) {
        return manualDubboService.searchManuals(param, buildCtx());
    }

    private UserContextDTO buildCtx() {
        return new UserContextDTO(UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles(), UserContext.getPermissions());
    }
}
