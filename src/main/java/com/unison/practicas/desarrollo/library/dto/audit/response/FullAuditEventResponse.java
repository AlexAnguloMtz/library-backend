package com.unison.practicas.desarrollo.library.dto.audit.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FullAuditEventResponse(
        String id,
        String responsibleId,
        String responsibleFirstName,
        String responsibleLastName,
        String responsibleProfilePictureUrl,
        String eventType,
        String resourceType,
        String eventData,
        String eventDataPretty,
        LocalDateTime occurredAt
) {
}