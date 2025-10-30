package com.unison.practicas.desarrollo.library.service.audit;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.entity.audit.AuditEventEntity;
import com.unison.practicas.desarrollo.library.entity.audit.AuditEventType;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.AuditEventRepository;
import com.unison.practicas.desarrollo.library.repository.AuditEventTypeRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.AuthenticationUtils;
import com.unison.practicas.desarrollo.library.util.JsonUtils;
import com.unison.practicas.desarrollo.library.util.StringHelper;
import com.unison.practicas.desarrollo.library.util.event.AuditEvent;
import com.unison.practicas.desarrollo.library.util.event.UserLoggedIn;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
class AuditLogEventHandler {

    private final AuditEventTypeRepository auditEventTypeRepository;
    private final AuditEventRepository auditEventRepository;
    private final AuthenticationUtils authUtils;
    private final JsonUtils jsonUtils;
    private final UserRepository userRepository;

    AuditLogEventHandler(AuditEventTypeRepository auditEventTypeRepository, AuditEventRepository auditEventRepository, AuthenticationUtils authUtils, JsonUtils jsonUtils, UserRepository userRepository) {
        this.auditEventTypeRepository = auditEventTypeRepository;
        this.auditEventRepository = auditEventRepository;
        this.authUtils = authUtils;
        this.jsonUtils = jsonUtils;
        this.userRepository = userRepository;
    }

    @EventListener
    void handle(AuditEvent event) {
        AuditEventEntity eventEntity = toEntity(event);
        auditEventRepository.save(eventEntity);
    }

    private AuditEventEntity toEntity(AuditEvent event) {
        Optional<User> currentUser = getCurrentUser(event);
        if (currentUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String eventTypeId = StringHelper.pascalCaseToSnakeCase(event.getClass().getSimpleName()).toUpperCase();

        AuditEventType eventType = auditEventTypeRepository.findById(eventTypeId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Could not find event type with id: %s".formatted(eventTypeId))
        );

        var eventEntity = new AuditEventEntity();
        eventEntity.setEventType(eventType);
        eventEntity.setResponsible(currentUser.get());
        eventEntity.setEventData(jsonUtils.toJson(event));
        eventEntity.setOccurredAt(LocalDateTime.now());

        auditEventRepository.save(eventEntity);

        return eventEntity;
    }

    private Optional<User> getCurrentUser(AuditEvent event) {
        // The user logged in just now, so there is no user in the security context yet.
        // So in such case, we get the user with data from the login event
        if (event instanceof UserLoggedIn loginEvent) {
            return userRepository.findByEmailIgnoreCase(loginEvent.getEmail());
        }
        // If event is not a login event, we assume the user might be already logged in
        Optional<CustomUserDetails> userDetails = authUtils.getCurrentUser();
        return userDetails.map(CustomUserDetails::getUser);
    }

}