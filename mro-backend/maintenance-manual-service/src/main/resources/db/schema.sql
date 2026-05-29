-- MRO-004 Maintenance Manual Management Schema
-- Refs: MRO-004

CREATE TABLE IF NOT EXISTS manual_document (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title         VARCHAR(256) NOT NULL COMMENT '手册标题',
    manual_no     VARCHAR(64)  NOT NULL COMMENT '手册编号（唯一）',
    aircraft_type VARCHAR(64)  NOT NULL COMMENT '适用机型',
    format        VARCHAR(16)  NOT NULL COMMENT '格式: PDF/XML/SGML',
    file_url      VARCHAR(512) NOT NULL COMMENT 'MinIO 对象路径',
    parsed_status VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '解析状态: PENDING/PARSING/DONE/FAILED',
    published     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已发布',
    uploader_id   BIGINT       NOT NULL COMMENT '上传者用户 ID',
    deleted       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_manual_no (manual_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修手册文档';

CREATE TABLE IF NOT EXISTS manual_version (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    document_id     BIGINT       NOT NULL COMMENT '关联手册 ID',
    version_no      VARCHAR(32)  NOT NULL COMMENT '版本号',
    change_summary  VARCHAR(512) COMMENT '变更摘要',
    effective_date  DATE         COMMENT '生效日期',
    revised_by      BIGINT       NOT NULL COMMENT '修订人用户 ID',
    revised_by_name VARCHAR(64)  COMMENT '修订人姓名（冗余）',
    deleted         TINYINT(1)   NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_doc_version (document_id, version_no),
    KEY idx_document_id (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手册版本记录';

CREATE TABLE IF NOT EXISTS translation_task (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    document_id   BIGINT       NOT NULL COMMENT '关联手册 ID',
    source_lang   VARCHAR(8)   NOT NULL DEFAULT 'en' COMMENT '源语言',
    target_lang   VARCHAR(8)   NOT NULL DEFAULT 'zh' COMMENT '目标语言',
    status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '翻译状态: PENDING/PROCESSING/DONE/FAILED',
    accuracy_score DECIMAL(5,2) COMMENT '翻译准确率评分',
    result_url    VARCHAR(512) COMMENT '翻译结果 MinIO 路径',
    operator_id   BIGINT       NOT NULL COMMENT '提交人用户 ID',
    deleted       TINYINT(1)   NOT NULL DEFAULT 0,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_document_id (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='翻译任务';
