package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.book.request.MergePublishersRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.PublisherRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.MergePublishersResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.PublisherResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetPublishersRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.service.book.PublisherService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @GetMapping
    public PaginationResponse<PublisherResponse> getPublishers(
            @Valid GetPublishersRequest request,
            @Valid PaginationRequest pagination
    ) {
        return publisherService.getPublishers(request, pagination);
    }

    @PostMapping
    public PublisherResponse createPublisher(@Valid @RequestBody PublisherRequest request) {
        return publisherService.createPublisher(request);
    }

    @PutMapping("/{id}")
    public PublisherResponse updatePublisher(
            @PathVariable String id,
            @Valid @RequestBody PublisherRequest request
    ) {
        return publisherService.updatePublisher(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublisherById(@PathVariable String id) {
        publisherService.deletePublisherById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(
            @Valid @RequestBody ExportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ExportResponse response = publisherService.export(userDetails, request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.fileName())
                .contentType(response.mediaType())
                .body(response.fileBytes());
    }

    @PostMapping("/merge")
    public MergePublishersResponse merge(@Valid @RequestBody MergePublishersRequest request) {
        return publisherService.merge(request);
    }

}