package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.dto.audit.request.GetAuditEventRequest;
import com.unison.practicas.desarrollo.library.dto.audit.request.GetAuditEventsRequest;
import com.unison.practicas.desarrollo.library.dto.audit.response.AuditEventResponse;
import com.unison.practicas.desarrollo.library.dto.audit.response.AuditResourceTypeResponse;
import com.unison.practicas.desarrollo.library.dto.audit.response.FullAuditEventResponse;
import com.unison.practicas.desarrollo.library.service.audit.AuditService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/events")
    public PaginationResponse<AuditEventResponse> getAuditEvents(
            @Valid GetAuditEventsRequest filters,
            @Valid PaginationRequest pagination
    ) {
        return auditService.getAuditEvents(filters, pagination);
    }

    @GetMapping("/events/{id}")
    public FullAuditEventResponse getAuditEventById(
            @PathVariable String id,
            @Valid GetAuditEventRequest request
    ) {
        return auditService.getAuditEventById(id, request);
    }

    @GetMapping("/resource-types")
    public List<AuditResourceTypeResponse> getAuditResourceTypes() {
        return auditService.getAuditResourceTypes();
    }

}
