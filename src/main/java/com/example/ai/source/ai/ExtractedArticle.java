package com.example.ai.source.ai;

import java.time.LocalDateTime;

public record ExtractedArticle(
        String title,
        String content,
        String contentHash,
        LocalDateTime publishedAt
) { }