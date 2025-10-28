package com.unison.practicas.desarrollo.library.service.event;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.entity.audit.AuditEventEntity;
import com.unison.practicas.desarrollo.library.entity.audit.AuditEventType;
import com.unison.practicas.desarrollo.library.repository.AuditEventRepository;
import com.unison.practicas.desarrollo.library.repository.AuditEventTypeRepository;
import com.unison.practicas.desarrollo.library.util.AuthenticationUtils;
import com.unison.practicas.desarrollo.library.util.JsonUtils;
import com.unison.practicas.desarrollo.library.util.StringHelper;
import com.unison.practicas.desarrollo.library.util.events.AuditEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Component
class AuditLogEventHandler {

    private final AuditEventTypeRepository auditEventTypeRepository;
    private final AuditEventRepository auditEventRepository;
    private final AuthenticationUtils authUtils;
    private final JsonUtils jsonUtils;

    AuditLogEventHandler(AuditEventTypeRepository auditEventTypeRepository, AuditEventRepository auditEventRepository, AuthenticationUtils authUtils, JsonUtils jsonUtils) {
        this.auditEventTypeRepository = auditEventTypeRepository;
        this.auditEventRepository = auditEventRepository;
        this.authUtils = authUtils;
        this.jsonUtils = jsonUtils;
    }

    @EventListener
    void handle(AuditEvent event) {
        AuditEventEntity eventEntity = toEntity(event);
        auditEventRepository.save(eventEntity);
    }

    private AuditEventEntity toEntity(AuditEvent event) {
        CustomUserDetails currentUser = authUtils.getCurrentUser().orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)
        );

        String eventTypeId = StringHelper.pascalCaseToSnakeCase(event.getClass().getSimpleName()).toUpperCase();

        AuditEventType eventType = auditEventTypeRepository.findById(eventTypeId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        );

        var eventEntity = new AuditEventEntity();
        eventEntity.setEventType(eventType);
        eventEntity.setResponsible(currentUser.getUser());
        eventEntity.setEventData(jsonUtils.toJson(event));
        eventEntity.setOccurredAt(LocalDateTime.now());

        auditEventRepository.save(eventEntity);

        return eventEntity;
    }


}