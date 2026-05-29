package com.mro.common.dubbo.tshoot.request;

import java.io.Serializable;

public record UploadDocCommand(
        String title,
        String docType,
        String fileUrl,
        String contentHash
) implements Serializable {}
