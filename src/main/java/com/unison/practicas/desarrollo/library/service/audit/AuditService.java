package com.unison.practicas.desarrollo.library.service.audit;

import com.unison.practicas.desarrollo.library.dto.audit.request.GetAuditEventRequest;
import com.unison.practicas.desarrollo.library.dto.audit.request.GetAuditEventsRequest;
import com.unison.practicas.desarrollo.library.dto.audit.response.AuditEventResponse;
import com.unison.practicas.desarrollo.library.dto.audit.response.AuditResourceTypeResponse;
import com.unison.practicas.desarrollo.library.dto.audit.response.FullAuditEventResponse;
import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import com.unison.practicas.desarrollo.library.entity.audit.AuditEventEntity;
import com.unison.practicas.desarrollo.library.entity.audit.AuditEventType;
import com.unison.practicas.desarrollo.library.entity.audit.AuditResourceType;
import com.unison.practicas.desarrollo.library.repository.AuditEventRepository;
import com.unison.practicas.desarrollo.library.repository.AuditResourceTypeRepository;
import com.unison.practicas.desarrollo.library.service.user.ProfilePictureService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class AuditService {

    private final GetAuditEvents getAuditEvents;
    private final AuditResourceTypeRepository auditResourceTypeRepository;
    private final AuditEventRepository auditEventRepository;
    private final MessageSource auditMessageSource;
    private final ProfilePictureService profilePictureService;
    private final AuditEventFormatter eventDataFormatter;

    AuditService(
            GetAuditEvents getAuditEvents,
            AuditResourceTypeRepository auditResourceTypeRepository, AuditEventRepository auditEventRepository,
            @Qualifier("auditMessageSource") MessageSource auditMessageSource, ProfilePictureService profilePictureService, AuditEventFormatter eventDataFormatter
    ) {
        this.getAuditEvents = getAuditEvents;
        this.auditResourceTypeRepository = auditResourceTypeRepository;
        this.auditEventRepository = auditEventRepository;
        this.auditMessageSource = auditMessageSource;
        this.profilePictureService = profilePictureService;
        this.eventDataFormatter = eventDataFormatter;
    }

    @PreAuthorize("hasAuthority('audit-events:read')")
    public PaginationResponse<AuditEventResponse> getAuditEvents(
            GetAuditEventsRequest filters,
            PaginationRequest pagination
    ) {
        return getAuditEvents.get(filters, pagination);
    }

    @PreAuthorize("hasAuthority('audit-events:read')")
    public FullAuditEventResponse getAuditEventById(String id, GetAuditEventRequest request) {
        AuditEventEntity event = auditEventRepository.findById(Integer.parseInt(id)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find event with id: %s".formatted(id)));

        return toFullResponse(event, request);
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

    private FullAuditEventResponse toFullResponse(AuditEventEntity event, GetAuditEventRequest request) {
        return FullAuditEventResponse.builder()
                .id(event.getId().toString())
                .occurredAt(event.getOccurredAt())
                .responsibleId(event.getResponsible().getId().toString())
                .responsibleFirstName(event.getResponsible().getFirstName())
                .responsibleLastName(event.getResponsible().getLastName())
                .responsibleProfilePictureUrl(profilePictureService.profilePictureUrl(event.getResponsible().getProfilePictureUrl().orElse(null)))
                .eventType(translate(event.getEventType().getId()))
                .resourceType(translate(event.getEventType().getResourceType().getId()))
                .eventData(event.getEventData())
                .eventDataPretty(request.eventDataPretty() ? eventDataFormatter.format(event) : "")
                .build();
    }

    private String translate(String text) {
        return auditMessageSource.getMessage(text, null, text, null);
    }

}