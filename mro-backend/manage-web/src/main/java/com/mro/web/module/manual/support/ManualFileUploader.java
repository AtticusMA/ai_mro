package com.mro.web.module.manual.support;

import com.mro.web.config.ManualMinioConfig;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ManualFileUploader {

    private final MinioClient manualMinioClient;
    private final ManualMinioConfig.MinioProperties manualMinioProperties;

    public String upload(MultipartFile file, String manualNo) {
        String ext = getExtension(file.getOriginalFilename());
        String objectKey = "manuals/" + manualNo + "/" + UUID.randomUUID() + ext;
        try {
            ensureBucketExists();
            manualMinioClient.putObject(PutObjectArgs.builder()
                    .bucket(manualMinioProperties.getBucketName())
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Manual file upload failed", e);
        }
        return objectKey;
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = manualMinioClient.bucketExists(
                BucketExistsArgs.builder().bucket(manualMinioProperties.getBucketName()).build());
        if (!exists) {
            manualMinioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(manualMinioProperties.getBucketName()).build());
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}
