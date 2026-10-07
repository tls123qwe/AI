package com.example.ai.source.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_sources")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String rawContent;

    @Column(length = 64)
    private String contentHash;

    private LocalDateTime publishedAt;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> keyPoints = new ArrayList<>();

    private String category;

    @Column(columnDefinition = "TEXT")
    private String approvedSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SourceStatus status;

    private String failReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static LearningSource create(String url) {
        LearningSource source = new LearningSource();
        source.url = url;
        source.status = SourceStatus.SUBMITTED;
        return source;
    }

    public void startProcessing() {
        validateStatus(SourceStatus.SUBMITTED);
        this.status = SourceStatus.PROCESSING;
    }

    public void completeExtraction(String title, String rawContent, String contentHash, LocalDateTime publishedAt) {
        validateStatus(SourceStatus.PROCESSING);
        this.title = title;
        this.rawContent = rawContent;
        this.contentHash = contentHash;
        this.publishedAt = publishedAt;
    }

    public void completeSummary(String aiSummary, List<String> keyPoints, String category) {
        validateStatus(SourceStatus.PROCESSING);
        this.aiSummary = aiSummary;
        this.keyPoints = new ArrayList<>(keyPoints);
        this.category = category;
        this.status = SourceStatus.PENDING_REVIEW;
    }

    public void fail(String reason) {
        this.status = SourceStatus.FAILED;
        this.failReason = reason;
    }

    private void validateStatus(SourceStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "현재 상태(" + status + ")에서는 처리할 수 없습니다. 필요 상태: " + expected);
        }
    }
}