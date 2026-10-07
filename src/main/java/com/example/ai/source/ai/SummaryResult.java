package com.example.ai.source.ai;

import java.util.List;

public record SummaryResult(
        String summary,
        List<String> keyPoints,
        String category
) { }