package com.mro.manual.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.manual.request.ManualSearchParam;
import com.mro.common.dubbo.manual.response.ManualSearchResultDTO;
import com.mro.manual.domain.es.ManualContentDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualSearchService {

    private final ElasticsearchOperations esOperations;

    public PageResult<ManualSearchResultDTO> search(ManualSearchParam param) {
        int from = (param.pageNum() - 1) * param.pageSize();

        var query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    b.must(m -> m.match(mm -> mm
                            .field("content")
                            .query(param.query())));
                    if (param.aircraftType() != null && !param.aircraftType().isBlank()) {
                        b.filter(f -> f.term(t -> t
                                .field("aircraftType")
                                .value(param.aircraftType())));
                    }
                    return b;
                }))
                .withHighlightQuery(new HighlightQuery(
                        new Highlight(
                                HighlightParameters.builder()
                                        .withPreTags("<em>")
                                        .withPostTags("</em>")
                                        .build(),
                                List.of(new HighlightField("content"))
                        ),
                        ManualContentDoc.class
                ))
                .withPageable(PageRequest.of(param.pageNum() - 1, param.pageSize()));

        SearchHits<ManualContentDoc> hits = esOperations.search(query.build(), ManualContentDoc.class);

        List<ManualSearchResultDTO> results = hits.getSearchHits().stream()
                .map(hit -> {
                    ManualContentDoc doc = hit.getContent();
                    String highlight = extractHighlight(hit);
                    return new ManualSearchResultDTO(
                            doc.documentId(),
                            doc.manualNo(),
                            doc.chapterRef(),
                            highlight,
                            hit.getScore()
                    );
                })
                .toList();

        return PageResult.of(results, hits.getTotalHits(), param.pageNum(), param.pageSize());
    }

    private String extractHighlight(SearchHit<ManualContentDoc> hit) {
        List<String> fragments = hit.getHighlightField("content");
        if (fragments != null && !fragments.isEmpty()) {
            return String.join("...", fragments);
        }
        return hit.getContent().content().substring(0, Math.min(200, hit.getContent().content().length()));
    }
}
