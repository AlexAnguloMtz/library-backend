package com.unison.practicas.desarrollo.library.service.event;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.entity.audit.DomainEvent;
import com.unison.practicas.desarrollo.library.entity.audit.AuditEvent;
import com.unison.practicas.desarrollo.library.entity.audit.AuditEventType;
import com.unison.practicas.desarrollo.library.repository.AuditEventRepository;
import com.unison.practicas.desarrollo.library.repository.AuditEventTypeRepository;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
class AuditLogDomainEventHandler {

    private final AuditEventTypeRepository auditEventTypeRepository;
    private final AuditEventRepository auditEventRepository;

    AuditLogDomainEventHandler(AuditEventTypeRepository auditEventTypeRepository, AuditEventRepository auditEventRepository) {
        this.auditEventTypeRepository = auditEventTypeRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @EventListener
    void handle(DomainEvent domainEvent) {
        AuditEvent auditEvent = toAuditEvent(domainEvent);
        auditEventRepository.save(auditEvent);
    }

    private AuditEvent toAuditEvent(DomainEvent domainEvent) {
        CustomUserDetails currentUser = getCurrentUser().orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)
        );

        AuditEventType eventType = auditEventTypeRepository.findById(domainEvent.getEventTypeId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Could not find event type with id: %s".formatted(domainEvent.getEventTypeId()))
        );

        var auditEvent = new AuditEvent();
        auditEvent.setEventType(eventType);
        auditEvent.setResponsible(currentUser.getUser());
        auditEvent.setOccurredAt(LocalDateTime.now());
        auditEvent.setResourceId(domainEvent.getResourceId());
        auditEventRepository.save(auditEvent);
        return auditEvent;
    }

    private Optional<CustomUserDetails> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails);
        }
        return Optional.empty();
    }

}