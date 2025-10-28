package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.GetAuditEventsRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.AuditEventResponse;
import com.unison.practicas.desarrollo.library.service.user.ProfilePictureService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.unison.practicas.desarrollo.library.jooq.tables.AuditEvent.AUDIT_EVENT;
import static com.unison.practicas.desarrollo.library.jooq.tables.AuditEventType.AUDIT_EVENT_TYPE;
import static com.unison.practicas.desarrollo.library.jooq.tables.AuditResourceType.AUDIT_RESOURCE_TYPE;
import static com.unison.practicas.desarrollo.library.jooq.tables.AppUser.APP_USER;

@Component
public class GetAuditEvents {

    private final DSLContext dsl;
    private final ProfilePictureService profilePictureService;
    private final MessageSource auditMessageSource;

    public GetAuditEvents(
            DSLContext dsl,
            ProfilePictureService profilePictureService,
            @Qualifier("auditTranslationsMessageSource") MessageSource auditMessageSource
    ) {
        this.dsl = dsl;
        this.profilePictureService = profilePictureService;
        this.auditMessageSource = auditMessageSource;
    }

    public PaginationResponse<AuditEventResponse> get(GetAuditEventsRequest filters, PaginationRequest pagination) {

        var baseQuery = dsl.select(
                        AUDIT_EVENT.ID,
                        AUDIT_EVENT.RESOURCE_ID,
                        AUDIT_EVENT.OCCURRED_AT,
                        APP_USER.ID.as("responsible_id"),
                        APP_USER.FIRST_NAME,
                        APP_USER.LAST_NAME,
                        APP_USER.PROFILE_PICTURE_URL,
                        AUDIT_EVENT_TYPE.ID.as("event_type"),
                        AUDIT_RESOURCE_TYPE.ID.as("resource_type")
                )
                .from(AUDIT_EVENT)
                .join(APP_USER).on(APP_USER.ID.eq(AUDIT_EVENT.RESPONSIBLE_ID))
                .join(AUDIT_EVENT_TYPE).on(AUDIT_EVENT_TYPE.ID.eq(AUDIT_EVENT.EVENT_TYPE_ID))
                .join(AUDIT_RESOURCE_TYPE).on(AUDIT_RESOURCE_TYPE.ID.eq(AUDIT_EVENT_TYPE.RESOURCE_TYPE_ID));

        // Fuzzy search por resourceId
        if (filters.resourceId() != null && !filters.resourceId().isBlank()) {
            String pattern = "%" + filters.resourceId() + "%";
            baseQuery.where(AUDIT_EVENT.RESOURCE_ID.likeIgnoreCase(pattern));
        }

        // Fuzzy search por responsible (nombre, apellido)
        if (filters.responsible() != null && !filters.responsible().isBlank()) {
            String pattern = "%" + filters.responsible() + "%";
            baseQuery.where(
                    DSL.cast(APP_USER.ID, String.class).like(pattern)
                    .or(APP_USER.FIRST_NAME.likeIgnoreCase(pattern))
                    .or(APP_USER.LAST_NAME.likeIgnoreCase(pattern))
            );
        }

        // Filtros opcionales por resourceType y eventType exacto
        if (filters.resourceType() != null && !filters.resourceType().isBlank()) {
            baseQuery.where(AUDIT_RESOURCE_TYPE.ID.eq(filters.resourceType()));
        }
        if (filters.eventType() != null && !filters.eventType().isBlank()) {
            baseQuery.where(AUDIT_EVENT_TYPE.ID.eq(filters.eventType()));
        }

        // Obtener total items antes de paginar
        int totalItems = baseQuery.fetch().size();

        // Aplicar paginacion
        int offset = pagination.page() * pagination.size();
        var records = baseQuery
                .orderBy(AUDIT_EVENT.OCCURRED_AT.desc())
                .limit(pagination.size())
                .offset(offset)
                .fetch();

        List<AuditEventResponse> items = records.stream()
                .map(this::mapRecordToResponse)
                .toList();

        int totalPages = (int) Math.ceil((double) totalItems / pagination.size());

        return PaginationResponse.<AuditEventResponse>builder()
                .items(items)
                .page(pagination.page())
                .size(pagination.size())
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasPrevious(pagination.page() > 0)
                .hasNext(pagination.page() < totalPages - 1)
                .build();
    }

    private AuditEventResponse mapRecordToResponse(Record r) {
        return AuditEventResponse.builder()
                .id(r.get(AUDIT_EVENT.ID).toString())
                .resourceId(r.get(AUDIT_EVENT.RESOURCE_ID))
                .occurredAt(r.get(AUDIT_EVENT.OCCURRED_AT))
                .responsibleId(r.get("responsible_id", String.class))
                .responsibleFirstName(r.get(APP_USER.FIRST_NAME))
                .responsibleLastName(r.get(APP_USER.LAST_NAME))
                .responsibleProfilePictureUrl(profilePictureService.profilePictureUrl(r.get(APP_USER.PROFILE_PICTURE_URL)))
                .eventType(translate(r.get("event_type", String.class)))
                .resourceType(translate(r.get("resource_type", String.class)))
                .build();
    }

    private String translate(String text) {
        return auditMessageSource.getMessage(text, null, text, null);
    }

}
