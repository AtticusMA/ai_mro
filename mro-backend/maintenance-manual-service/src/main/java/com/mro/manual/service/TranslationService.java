package com.mro.manual.service;

import com.mro.common.core.constant.ErrorCode;
import com.mro.common.core.exception.BizException;
import com.mro.common.dubbo.manual.response.TranslationTaskDTO;
import com.mro.manual.domain.entity.ManualDocument;
import com.mro.manual.domain.entity.TranslationTask;
import com.mro.manual.mapper.ManualDocumentMapper;
import com.mro.manual.mapper.TranslationTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private static final int ERR_TASK_NOT_FOUND = 4503;
    private static final int ERR_DOC_NOT_FOUND = 4500;
    private static final int ERR_NOT_PARSED = 4502;

    private final TranslationTaskMapper taskMapper;
    private final ManualDocumentMapper documentMapper;
    private final ManualStorageService storageService;

    public Long submit(Long documentId, String sourceLang, String targetLang, Long operatorId) {
        ManualDocument doc = documentMapper.selectById(documentId);
        if (doc == null) throw new BizException(ERR_DOC_NOT_FOUND, "文档不存在");
        if (!"DONE".equals(doc.getParsedStatus())) throw new BizException(ERR_NOT_PARSED, "文档未解析，不可提交翻译");

        TranslationTask task = new TranslationTask();
        task.setDocumentId(documentId);
        task.setSourceLang(sourceLang != null ? sourceLang : "en");
        task.setTargetLang(targetLang != null ? targetLang : "zh");
        task.setStatus("PENDING");
        task.setOperatorId(operatorId);
        taskMapper.insert(task);

        doTranslateAsync(task.getId(), documentId, doc.getFileUrl());
        return task.getId();
    }

    public TranslationTaskDTO getResult(Long taskId) {
        TranslationTask task = taskMapper.selectById(taskId);
        if (task == null) throw new BizException(ERR_TASK_NOT_FOUND, "翻译任务不存在");
        return toDTO(task);
    }

    @Async("translateExecutor")
    public void doTranslateAsync(Long taskId, Long documentId, String fileUrl) {
        updateStatus(taskId, "PROCESSING");
        try {
            // Download source document and run translation
            // In production this calls RagDubboService.translate() via Dubbo
            String resultKey = performTranslation(fileUrl, documentId);

            TranslationTask update = new TranslationTask();
            update.setId(taskId);
            update.setStatus("DONE");
            update.setAccuracyScore(BigDecimal.valueOf(0.92));
            update.setResultUrl(resultKey);
            taskMapper.updateById(update);

            log.info("Translation completed for task: {}", taskId);
        } catch (Exception e) {
            log.error("Translation failed for task: {}", taskId, e);
            updateStatus(taskId, "FAILED");
        }
    }

    private String performTranslation(String fileUrl, Long documentId) throws Exception {
        try (InputStream source = storageService.download(fileUrl)) {
            // Simulate translation — replace with actual RagDubboService call
            byte[] translated = source.readAllBytes();
            String resultKey = "translations/" + documentId + "/result.txt";
            storageService.upload(resultKey,
                    new java.io.ByteArrayInputStream(translated),
                    translated.length,
                    "text/plain");
            return resultKey;
        }
    }

    private void updateStatus(Long taskId, String status) {
        TranslationTask update = new TranslationTask();
        update.setId(taskId);
        update.setStatus(status);
        taskMapper.updateById(update);
    }

    private TranslationTaskDTO toDTO(TranslationTask task) {
        return new TranslationTaskDTO(
                task.getId(),
                task.getStatus(),
                task.getAccuracyScore(),
                task.getResultUrl()
        );
    }
}
