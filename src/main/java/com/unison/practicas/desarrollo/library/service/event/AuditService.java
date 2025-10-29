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
import com.unison.practicas.desarrollo.library.service.user.ProfilePictureService;
import com.unison.practicas.desarrollo.library.util.JsonUtils;
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
    private final JsonUtils jsonUtils;

    public AuditService(
            GetAuditEvents getAuditEvents,
            AuditResourceTypeRepository auditResourceTypeRepository, AuditEventRepository auditEventRepository,
            @Qualifier("auditMessageSource") MessageSource auditMessageSource, ProfilePictureService profilePictureService, JsonUtils jsonUtils
    ) {
        this.getAuditEvents = getAuditEvents;
        this.auditResourceTypeRepository = auditResourceTypeRepository;
        this.auditEventRepository = auditEventRepository;
        this.auditMessageSource = auditMessageSource;
        this.profilePictureService = profilePictureService;
        this.jsonUtils = jsonUtils;
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

        return toFullResponse(event);
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

    private FullAuditEventResponse toFullResponse(AuditEventEntity event) {
        return FullAuditEventResponse.builder()
                .id(event.getId().toString())
                .occurredAt(event.getOccurredAt())
                .responsibleId(event.getResponsible().getId().toString())
                .responsibleFirstName(event.getResponsible().getFirstName())
                .responsibleLastName(event.getResponsible().getLastName())
                .responsibleProfilePictureUrl(profilePictureService.profilePictureUrl(event.getResponsible().getProfilePictureUrl().orElse(null)))
                .eventType(translate(event.getEventType().getId()))
                .resourceType(translate(event.getEventType().getResourceType().getId()))
                .eventData(translateEventData(event.getEventType().getId(), event.getEventData()))
                .build();
    }

    private String translateEventData(String eventTypeId, String eventData) {
        Map<String, Object> originalMap = jsonUtils.fromJson(eventData, Map.class);
        Map<String, Object> translatedMap = new LinkedHashMap<>();

        Deque<StackFrame> stack = new ArrayDeque<>();
        stack.push(new StackFrame(originalMap, translatedMap, null));

        while (!stack.isEmpty()) {
            StackFrame frame = stack.pop();

            for (Map.Entry<String, Object> entry : frame.original.entrySet()) {
                String fullKey = frame.parentKey == null ? entry.getKey() : frame.parentKey + "." + entry.getKey();
                String translatedKey = translate(eventTypeId + "." + fullKey);
                Object value = entry.getValue();

                if (value instanceof Map) {
                    Map<String, Object> newTranslated = new LinkedHashMap<>();
                    frame.translated.put(translatedKey, newTranslated);
                    // Añadir a la pila para procesar el sub-map
                    stack.push(new StackFrame((Map<String, Object>) value, newTranslated, fullKey));
                } else {
                    frame.translated.put(translatedKey, value);
                }
            }
        }

        return jsonUtils.toJson(translatedMap);
    }

    private static class StackFrame {
        Map<String, Object> original;
        Map<String, Object> translated;
        String parentKey;

        StackFrame(Map<String, Object> original, Map<String, Object> translated, String parentKey) {
            this.original = original;
            this.translated = translated;
            this.parentKey = parentKey;
        }
    }

    private String translate(String text) {
        return auditMessageSource.getMessage(text, null, text, null);
    }

}