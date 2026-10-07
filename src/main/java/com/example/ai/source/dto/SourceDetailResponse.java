package com.example.ai.source.dto;

import com.example.ai.source.entity.LearningSource;
import com.example.ai.source.entity.SourceStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SourceDetailResponse(
        Long id, String url, String title, String category, SourceStatus status,
        LocalDateTime publishedAt, String aiSummary, List<String> keyPoints,
        String rawContent, String failReason, LocalDateTime createdAt
) {
    public static SourceDetailResponse from(LearningSource s) {
        return new SourceDetailResponse(s.getId(), s.getUrl(), s.getTitle(), s.getCategory(),
                s.getStatus(), s.getPublishedAt(), s.getAiSummary(), List.copyOf(s.getKeyPoints()),
                s.getRawContent(), s.getFailReason(), s.getCreatedAt());
    }
}