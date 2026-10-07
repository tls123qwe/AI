package com.example.ai.source.service;

import com.example.ai.source.ai.*;
import com.example.ai.source.entity.LearningSource;
import com.example.ai.source.event.SourceRegisteredEvent;
import com.example.ai.source.repository.LearningSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceProcessor {

    private final LearningSourceRepository sourceRepository;
    private final ArticleExtractor articleExtractor;
    private final ArticleSummarizer articleSummarizer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SourceRegisteredEvent event) {
        LearningSource source = sourceRepository.findById(event.sourceId()).orElse(null);
        if (source == null) {
            log.warn("처리할 소스를 찾을 수 없습니다. id={}", event.sourceId());
            return;
        }

        try {
            source.startProcessing();
            source = sourceRepository.save(source);

            ExtractedArticle article = articleExtractor.extract(source.getUrl());
            if (sourceRepository.existsByContentHashAndIdNot(article.contentHash(), source.getId())) {
                source.fail("이미 등록된 내용과 동일한 글입니다.");
                sourceRepository.save(source);
                return;
            }
            source.completeExtraction(article.title(), article.content(),
                    article.contentHash(), article.publishedAt());
            source = sourceRepository.save(source);

            SummaryResult summary = articleSummarizer.summarize(article.title(), article.content());
            source.completeSummary(summary.summary(), summary.keyPoints(), summary.category());
            sourceRepository.save(source);

        } catch (ArticleExtractionException e) {
            source.fail(e.getMessage());
            sourceRepository.save(source);
        } catch (Exception e) {
            log.error("소스 처리 실패. id={}", source.getId(), e);
            source.fail("처리 중 오류가 발생했습니다: " + e.getMessage());
            sourceRepository.save(source);
        }
    }
}