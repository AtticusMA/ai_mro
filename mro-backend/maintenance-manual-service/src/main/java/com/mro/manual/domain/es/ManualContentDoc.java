package com.mro.manual.domain.es;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "manual_content")
public record ManualContentDoc(
        @Id
        String id,

        @Field(type = FieldType.Long)
        Long documentId,

        @Field(type = FieldType.Keyword)
        String manualNo,

        @Field(type = FieldType.Keyword)
        String aircraftType,

        @Field(type = FieldType.Keyword)
        String chapterRef,

        @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
        String content
) {}
