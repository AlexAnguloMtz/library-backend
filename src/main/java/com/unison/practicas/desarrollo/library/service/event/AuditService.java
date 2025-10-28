package com.unison.practicas.desarrollo.library.service.event;

import com.unison.practicas.desarrollo.library.dto.book.request.GetAuditEventsRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.AuditEventResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.AuditResourceTypeResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.FullAuditEventResponse;
import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import com.unison.practicas.desarrollo.library.entity.audit.AuditEventEntity;
import com.unison.practicas.desarrollo.library.entity.audit.AuditEventType;
import com.unison.practicas.desarrollo.library.entity.audit.AuditResourceType;
import com.unison.practicas.desarrollo.library.repository.AuditEventRepository;
import com.unison.practicas.desarrollo.library.repository.AuditResourceTypeRepository;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuditService {

    private final GetAuditEvents getAuditEvents;
    private final AuditResourceTypeRepository auditResourceTypeRepository;
    private final AuditEventRepository auditEventRepository;
    private final MessageSource auditMessageSource;

    public AuditService(
            GetAuditEvents getAuditEvents,
            AuditResourceTypeRepository auditResourceTypeRepository, AuditEventRepository auditEventRepository,
            @Qualifier("auditTranslationsMessageSource") MessageSource auditMessageSource
    ) {
        this.getAuditEvents = getAuditEvents;
        this.auditResourceTypeRepository = auditResourceTypeRepository;
        this.auditEventRepository = auditEventRepository;
        this.auditMessageSource = auditMessageSource;
    }

    @PreAuthorize("hasAuthority('audit-events:read')")
    public PaginationResponse<AuditEventResponse> getAuditEvents(
            GetAuditEventsRequest filters,
            PaginationRequest pagination
    ) {
        return getAuditEvents.get(filters, pagination);
    }

    @PreAuthorize("hasAuthority('audit-events:read')")
    public FullAuditEventResponse getAuditEventById(String id) {
        AuditEventEntity event = auditEventRepository.findById(Integer.parseInt(id)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find event with id: %s".formatted(id)));

        // TODO
        //  Implement
        return null;
    }

    @PreAuthorize("hasAuthority('audit-events:read')")
    public List<AuditResourceTypeResponse> getAuditResourceTypes() {
        return auditResourceTypeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditResourceTypeResponse toResponse(AuditResourceType entity) {
        return AuditResourceTypeResponse.builder()
                .id(entity.getId())
                .name(translate(entity.getId()))
                .eventTypes(entity.getEventTypes().stream().map(this::toOptionResponse).toList())
                .build();
    }

    private OptionResponse toOptionResponse(AuditEventType entity) {
        return OptionResponse.builder()
                .value(entity.getId())
                .label(translate(entity.getId()))
                .build();
    }

    private String translate(String text) {
        return auditMessageSource.getMessage(text, null, text, null);
    }


}