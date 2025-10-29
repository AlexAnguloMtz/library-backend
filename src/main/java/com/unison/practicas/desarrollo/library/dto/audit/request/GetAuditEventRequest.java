package com.unison.practicas.desarrollo.library.dto.audit.request;

import lombok.Builder;

@Builder
public record GetAuditEventRequest(
        Boolean eventDataPretty
) {
}