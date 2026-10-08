package com.example.ai.source.service;

import com.example.ai.source.dto.*;
import com.example.ai.source.entity.LearningSource;
import com.example.ai.source.entity.SourceStatus;
import com.example.ai.source.event.SourceRegisteredEvent;
import com.example.ai.source.repository.LearningSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SourceService {

    private final LearningSourceRepository sourceRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SourceResponse register(SourceCreateRequest request) {
        String url = request.url().strip();
        if (sourceRepository.existsByUrl(url)) {
            throw new IllegalStateException("이미 등록된 링크입니다.");
        }

        LearningSource source = sourceRepository.save(LearningSource.create(url));
        eventPublisher.publishEvent(new SourceRegisteredEvent(source.getId()));
        return SourceResponse.from(source);
    }

    public List<SourceResponse> findAll(SourceStatus status) {
        List<LearningSource> sources = (status == null)
                ? sourceRepository.findAllByOrderByCreatedAtDesc()
                : sourceRepository.findAllByStatusOrderByCreatedAtDesc(status);
        return sources.stream().map(SourceResponse::from).toList();
    }

    public SourceDetailResponse findById(Long id) {
        return sourceRepository.findById(id)
                .map(SourceDetailResponse::from)
                .orElseThrow(() -> new NoSuchElementException("소스를 찾을 수 없습니다. id=" + id));
    }
}