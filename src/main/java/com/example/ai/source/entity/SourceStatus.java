package com.example.ai.source.entity;

public enum SourceStatus {
    SUBMITTED,       // 링크 등록됨
    PROCESSING,      // 본문 추출 및 요약 중
    PENDING_REVIEW,  // 관리자 검토 대기
    LEARNED,         // 승인되어 학습 완료
    REJECTED,        // 관리자가 거절
    FAILED,          // 추출 또는 요약 실패
    REVOKED          // 학습 취소
}