package com.mro.common.dubbo.manual.response;

import java.io.Serializable;

public record ManualSearchResultDTO(
        Long documentId,
        String manualNo,
        String chapterRef,
        String highlight,
        double score
) implements Serializable {}
