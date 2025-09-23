package com.unison.practicas.desarrollo.library.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unison.practicas.desarrollo.library.dto.RoleResponse;
import com.unison.practicas.desarrollo.library.dto.UserPreview;
import com.unison.practicas.desarrollo.library.dto.UserPreviewsQuery;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import com.unison.practicas.desarrollo.library.util.pagination.SortRequest;
import com.unison.practicas.desarrollo.library.util.pagination.SortingOrder;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Comparator;

import static com.unison.practicas.desarrollo.library.jooq.Tables.*;

@Service
public class UserService {

    private static final Set<String> ALLOWED_SORTS = Set.of(
            "firstName",
            "lastName",
            "email",
            "phoneNumber",
            "registrationDate",
            "role"
    );

    private static final List<SortRequest> DEFAULT_SORTS = List.of(
            new SortRequest("lastName", SortingOrder.ASC),
            new SortRequest("firstName", SortingOrder.ASC)
    );

    private final DSLContext dsl;
    private final DateTimeFormatter dateTimeFormatter;
    private final ObjectMapper objectMapper;

    public UserService(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = dsl;
        this.objectMapper = objectMapper;
        this.dateTimeFormatter = createDateTimeFormatter();
    }

    public PaginationResponse<UserPreview> getUsersPreviews(UserPreviewsQuery query, PaginationRequest paginationRequest) {
        // Aggregate user roles as a json
        Field<JSON> rolesJson = DSL.jsonArrayAgg(
                DSL.jsonObject(
                        DSL.jsonEntry("id", APP_ROLE.ID),
                        DSL.jsonEntry("name", APP_ROLE.NAME),
                        DSL.jsonEntry("slug", APP_ROLE.SLUG)
                )
        ).as("roles_json");

        // Roles names concatenation
        Field<String> roleNamesConcat = DSL.groupConcat(APP_ROLE.NAME)
                .orderBy(APP_ROLE.NAME.asc())
                .separator(",")
                .as("roles_concat");

        // Base query
        var base = dsl.select(
                        APP_USER.ID,
                        APP_USER.FIRST_NAME,
                        APP_USER.LAST_NAME,
                        APP_USER.EMAIL,
                        APP_USER.PHONE_NUMBER,
                        APP_USER.REGISTRATION_DATE,
                        rolesJson,
                        roleNamesConcat
                )
                .from(APP_USER)
                .join(USER_ROLE).on(APP_USER.ID.eq(USER_ROLE.USER_ID))
                .join(APP_ROLE).on(APP_ROLE.ID.eq(USER_ROLE.ROLE_ID));

        // Filters
        if (query.search() != null && !query.search().isBlank()) {
            String pattern = "%" + query.search().toLowerCase() + "%";
            base.where(DSL.lower(APP_USER.FIRST_NAME).like(pattern)
                    .or(DSL.lower(APP_USER.LAST_NAME).like(pattern))
                    .or(DSL.lower(APP_USER.EMAIL).like(pattern))
                    .or(DSL.lower(APP_USER.PHONE_NUMBER).like(pattern)));
        }

        if (!CollectionUtils.isEmpty(query.role())) {
            base.where(APP_ROLE.SLUG.in(query.role()));
        }

        if (query.registrationDateMin() != null) {
            base.where(APP_USER.REGISTRATION_DATE.ge(OffsetDateTime.from(query.registrationDateMin().atStartOfDay())));
        }

        if (query.registrationDateMax() != null) {
            base.where(APP_USER.REGISTRATION_DATE.le(OffsetDateTime.from(query.registrationDateMax().atTime(23, 59, 59))));
        }

        // Group by user
        base.groupBy(
                APP_USER.ID,
                APP_USER.FIRST_NAME,
                APP_USER.LAST_NAME,
                APP_USER.EMAIL,
                APP_USER.PHONE_NUMBER,
                APP_USER.REGISTRATION_DATE
        );

        // Count query (total items)
        long totalItems = dsl.selectCount()
                .from(base.asTable("count_sub"))
                .fetchOne(0, long.class);

        // Sorting
        List<SortRequest> sorts = CollectionUtils.isEmpty(paginationRequest.sorts())
                ? DEFAULT_SORTS
                : paginationRequest.sorts();

        for (SortRequest sortReq : sorts) {
            if (!ALLOWED_SORTS.contains(sortReq.sort())) continue;

            if ("role".equals(sortReq.sort())) {
                base.orderBy(SortingOrder.DESC.equals(sortReq.order())
                        ? roleNamesConcat.desc()
                        : roleNamesConcat.asc());
            } else {
                base.orderBy(orderField(sortReq));
            }
        }

        // Pagination
        int offset = paginationRequest.page() * paginationRequest.size();
        base.offset(offset).limit(paginationRequest.size());

        // Execute
        var result = base.fetch();

        // Map results
        List<UserPreview> items = result.stream().map(r -> new UserPreview(
                r.get(APP_USER.ID).toString(),
                formatInvertedName(r.get(APP_USER.FIRST_NAME), r.get(APP_USER.LAST_NAME)),
                r.get(APP_USER.EMAIL),
                r.get(APP_USER.PHONE_NUMBER),
                parseRolesJson(r.get("roles_json", String.class)),
                dateTimeFormatter.format(r.get(APP_USER.REGISTRATION_DATE)),
                "5"
        )).toList();

        long totalPages = (long) Math.ceil((double) totalItems / paginationRequest.size());

        return PaginationResponse.<UserPreview>builder()
                .items(items)
                .page(paginationRequest.page())
                .size(paginationRequest.size())
                .totalItems(totalItems)
                .totalPages((int) totalPages)
                .build();
    }

    private List<RoleResponse> parseRolesJson(String rolesJson) {
        try {
            List<Map<String, Object>> rolesList = objectMapper.readValue(
                    rolesJson,
                    new TypeReference<>() {}
            );

            return rolesList.stream()
                    .map(this::toRoleResponse)
                    .sorted(Comparator.comparing(RoleResponse::name))
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Could not read user roles correctly");
        }
    }

    private RoleResponse toRoleResponse(Map<String, Object> map) {
        return new RoleResponse(
                map.get("id").toString(),
                map.get("name").toString(),
                map.get("slug").toString()
        );
    }

    private org.jooq.SortField<?> orderField(SortRequest sortReq) {
        Field<?> field = switch (sortReq.sort()) {
            case "firstName" -> APP_USER.FIRST_NAME;
            case "email" -> APP_USER.EMAIL;
            case "phoneNumber" -> APP_USER.PHONE_NUMBER;
            case "registrationDate" -> APP_USER.REGISTRATION_DATE;
            default -> APP_USER.LAST_NAME;
        };
        return SortingOrder.DESC.equals(sortReq.order()) ? field.desc() : field.asc();
    }

    private String formatInvertedName(String firstName, String lastName) {
        return "%s, %s".formatted(lastName, firstName);
    }

    private DateTimeFormatter createDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(
                "dd/MMM/yyyy",
                new Locale.Builder()
                        .setLanguage("es")
                        .setRegion("MX")
                        .build()
        );
    }
}
