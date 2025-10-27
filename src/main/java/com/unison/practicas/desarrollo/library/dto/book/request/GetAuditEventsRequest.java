package com.unison.practicas.desarrollo.library.dto.book.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetAuditEventsRequest(

        @Size(max = 50)
        String responsible,

        @Size(max = 50)
        String resourceId,

        @Size(max = 30)
        String resourceType,

        @Size(max = 30)
        String eventType,

        LocalDateTime occurredAtMin,

        LocalDateTime occurredAtMax

) {
}