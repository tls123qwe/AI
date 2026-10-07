package com.example.ai.source.dto;

import com.example.ai.source.entity.LearningSource;
import com.example.ai.source.entity.SourceStatus;

import java.time.LocalDateTime;

public record SourceResponse(
        Long id, String url, String title, String category,
        SourceStatus status, String failReason, LocalDateTime createdAt
) {
    public static SourceResponse from(LearningSource s) {
        return new SourceResponse(s.getId(), s.getUrl(), s.getTitle(), s.getCategory(),
                s.getStatus(), s.getFailReason(), s.getCreatedAt());
    }
}