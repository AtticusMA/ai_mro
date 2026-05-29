package com.mro.manual.service;

import com.mro.manual.config.MinioConfig;
import io.minio.*;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualStorageService {

    private final MinioClient minioClient;
    private final MinioConfig.MinioProperties minioProperties;

    public void upload(String objectKey, InputStream inputStream, long size, String contentType) {
        try {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (MinioException e) {
            throw new RuntimeException("MinIO upload failed: " + objectKey, e);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    public InputStream download(String objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build());
        } catch (MinioException e) {
            throw new RuntimeException("MinIO download failed: " + objectKey, e);
        } catch (Exception e) {
            throw new RuntimeException("Download failed", e);
        }
    }

    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build());
        } catch (MinioException e) {
            log.warn("MinIO delete failed: {}", objectKey, e);
        } catch (Exception e) {
            log.warn("Delete failed for: {}", objectKey, e);
        }
    }

    public String buildObjectKey(String manualNo, String filename) {
        return "manuals/" + manualNo + "/" + filename;
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucketName())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build());
        }
    }
}
