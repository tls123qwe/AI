package com.example.ai.source.repository;

import com.example.ai.source.entity.LearningSource;
import com.example.ai.source.entity.SourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningSourceRepository extends JpaRepository<LearningSource, Long> {

    boolean existsByUrl(String url);

    boolean existsByContentHashAndIdNot(String contentHash, Long id);

    List<LearningSource> findAllByStatusOrderByCreatedAtDesc(SourceStatus status);

    List<LearningSource> findAllByOrderByCreatedAtDesc();
}