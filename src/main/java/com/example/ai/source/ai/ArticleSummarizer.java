package com.example.ai.source.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class ArticleSummarizer {

    private static final String SYSTEM_PROMPT = """
            너는 기사와 정보성 글을 관리자 검토용으로 정리하는 편집자다.
            규칙:
            1. 반드시 제공된 본문에 있는 내용만 사용한다. 사전 지식으로 내용을 보충하거나 추측하지 않는다.
            2. summary는 한국어 3~5문장으로 작성한다.
            3. keyPoints는 핵심 사실 3~7개를 각각 한 문장으로 작성한다.
            4. 수치, 날짜, 고유명사는 본문 표기 그대로 유지한다.
            5. category는 글의 분야를 한 단어로 작성한다.
            """;

    private final ChatClient chatClient;

    public ArticleSummarizer(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public SummaryResult summarize(String title, String content) {
        // 본문에 중괄호가 있으면 템플릿 렌더링이 깨질 수 있어서 문자열 결합 사용
        String userMessage = "제목: " + title + "\n\n본문:\n" + content;

        SummaryResult result = chatClient.prompt()
                .user(userMessage)
                .call()
                .entity(SummaryResult.class);

        if (result == null || result.summary() == null || result.summary().isBlank()) {
            throw new IllegalStateException("AI 요약 결과가 비어 있습니다.");
        }
        return result;
    }
}