package com.example.ai.source.controller;

import com.example.ai.source.dto.*;
import com.example.ai.source.entity.SourceStatus;
import com.example.ai.source.service.SourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sources")
@RequiredArgsConstructor
public class AdminSourceController {

    private final SourceService sourceService;

    @PostMapping
    public ResponseEntity<SourceResponse> register(@Valid @RequestBody SourceCreateRequest request) {
        return ResponseEntity.accepted().body(sourceService.register(request));
    }

    @GetMapping
    public ResponseEntity<List<SourceResponse>> findAll(@RequestParam(required = false) SourceStatus status) {
        return ResponseEntity.ok(sourceService.findAll(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceDetailResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(sourceService.findById(id));
    }
}