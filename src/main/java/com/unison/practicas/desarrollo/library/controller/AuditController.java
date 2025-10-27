package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.dto.book.request.GetAuditEventsRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.AuditEventResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.AuditResourceTypeResponse;
import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import com.unison.practicas.desarrollo.library.service.book.AuditService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/resource-types")
    public List<AuditResourceTypeResponse> getAuditResourceTypes() {
        return auditService.getAuditResourceTypes();
    }

}
