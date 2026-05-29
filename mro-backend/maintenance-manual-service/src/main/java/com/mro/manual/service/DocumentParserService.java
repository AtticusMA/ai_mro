package com.mro.manual.service;

import com.mro.manual.domain.entity.ManualDocument;
import com.mro.manual.domain.es.ManualContentDoc;
import com.mro.manual.mapper.ManualDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParserService {

    private final ManualStorageService storageService;
    private final ManualDocumentMapper documentMapper;
    private final ElasticsearchOperations esOperations;

    @Async("parseExecutor")
    public void parseAsync(Long documentId) {
        ManualDocument doc = documentMapper.selectById(documentId);
        if (doc == null) return;

        updateParsedStatus(documentId, "PARSING");
        try {
            String content = extractContent(doc.getFileUrl());
            indexToElasticsearch(doc, content);
            updateParsedStatus(documentId, "DONE");
            log.info("Parsed manual document: {}", documentId);
        } catch (Exception e) {
            log.error("Parse failed for document: {}", documentId, e);
            updateParsedStatus(documentId, "FAILED");
        }
    }

    private String extractContent(String fileUrl) throws Exception {
        try (InputStream inputStream = storageService.download(fileUrl)) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(inputStream, handler, metadata);
            return handler.toString();
        }
    }

    private void indexToElasticsearch(ManualDocument doc, String content) {
        ensureIndexExists();

        // Split content into chapter-sized chunks (~2000 chars each)
        List<String> chunks = splitIntoChunks(content, 2000);
        for (int i = 0; i < chunks.size(); i++) {
            String chapterRef = "CH-" + String.format("%03d", i + 1);
            ManualContentDoc esDoc = new ManualContentDoc(
                    doc.getId() + "-" + chapterRef,
                    doc.getId(),
                    doc.getManualNo(),
                    doc.getAircraftType(),
                    chapterRef,
                    chunks.get(i)
            );
            esOperations.save(esDoc);
        }
    }

    private void ensureIndexExists() {
        IndexOperations indexOps = esOperations.indexOps(ManualContentDoc.class);
        if (!indexOps.exists()) {
            indexOps.createWithMapping();
        }
    }

    private List<String> splitIntoChunks(String content, int chunkSize) {
        List<String> chunks = new java.util.ArrayList<>();
        for (int i = 0; i < content.length(); i += chunkSize) {
            chunks.add(content.substring(i, Math.min(i + chunkSize, content.length())));
        }
        if (chunks.isEmpty()) {
            chunks.add(content);
        }
        return chunks;
    }

    private void updateParsedStatus(Long documentId, String status) {
        ManualDocument update = new ManualDocument();
        update.setId(documentId);
        update.setParsedStatus(status);
        documentMapper.updateById(update);
    }
}
