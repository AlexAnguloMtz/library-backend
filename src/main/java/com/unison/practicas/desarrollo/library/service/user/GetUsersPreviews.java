package com.unison.practicas.desarrollo.library.service.user;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.user.response.RoleResponse;
import com.unison.practicas.desarrollo.library.dto.user.response.UserPreviewResponse;
import com.unison.practicas.desarrollo.library.dto.user.request.UserPreviewsRequest;
import com.unison.practicas.desarrollo.library.entity.RoleName;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import com.unison.practicas.desarrollo.library.util.pagination.SortRequest;
import com.unison.practicas.desarrollo.library.util.pagination.SortingOrder;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.unison.practicas.desarrollo.library.jooq.Tables.APP_ROLE;
import static com.unison.practicas.desarrollo.library.jooq.Tables.APP_USER;

// TODO
// Some users show 'Invalid Date' on the UI. Check why and fix.

@Component
public class GetUsersPreviews {

    private static final List<SortRequest> DEFAULT_SORTS = List.of(
            new SortRequest("lastName", SortingOrder.ASC),
            new SortRequest("firstName", SortingOrder.ASC)
    );

    private final DSLContext dsl;
    private final DateTimeFormatter dateTimeFormatter;
    private final ProfilePictureService profilePictureService;
    private final UserAuthorization userAuthorization;

    public GetUsersPreviews(DSLContext dsl, ProfilePictureService profilePictureService, UserAuthorization userAuthorization) {
        this.dsl = dsl;
        this.profilePictureService = profilePictureService;
        this.userAuthorization = userAuthorization;
        this.dateTimeFormatter = createDateTimeFormatter();
    }

    public PaginationResponse<UserPreviewResponse> handle(
            UserPreviewsRequest query,
            PaginationRequest paginationRequest,
            CustomUserDetails currentUser
    ) {

        // Base query: join directo a role
        var base = dsl.select(
                        APP_USER.ID,
                        APP_USER.FIRST_NAME,
                        APP_USER.LAST_NAME,
                        APP_USER.EMAIL,
                        APP_USER.PHONE_NUMBER,
                        APP_USER.PROFILE_PICTURE_URL,
                        APP_USER.REGISTRATION_DATE,
                        APP_ROLE.ID.as("role_id"),
                        APP_ROLE.NAME.as("role_name"),
                        APP_ROLE.SLUG.as("role_slug")
                )
                .from(APP_USER)
                .join(APP_ROLE).on(APP_USER.ROLE_ID.eq(APP_ROLE.ID));

        // Filters
        if (query.search() != null && !query.search().isBlank()) {
            String pattern = "%" + query.search().toLowerCase() + "%";
            base.where(
                DSL.cast(APP_USER.ID, String.class).like(pattern)
                    .or(DSL.lower(APP_USER.FIRST_NAME).like(pattern))
                    .or(DSL.lower(APP_USER.LAST_NAME).like(pattern))
                    .or(DSL.lower(APP_USER.EMAIL).like(pattern))
                    .or(DSL.lower(APP_USER.PHONE_NUMBER).like(pattern))
            );
        }

        if (!CollectionUtils.isEmpty(query.role())) {
            base.where(
                DSL.cast(APP_USER.ROLE_ID, String.class).in(query.role())
            );
        }

        if (query.registrationDateMin() != null) {
            base.where(APP_USER.REGISTRATION_DATE.ge(
                    query.registrationDateMin().atStartOfDay().atOffset(ZoneOffset.UTC)
            ));
        }

        if (query.registrationDateMax() != null) {
            base.where(APP_USER.REGISTRATION_DATE.le(
                    query.registrationDateMax().atTime(23, 59, 59).atOffset(ZoneOffset.UTC)
            ));
        }

        // Filter by listing permissions for current user role
        Set<RoleName> allowedRoles = userAuthorization.listableRoles(currentUser);

        Set<String> allowedRoleSlugs = allowedRoles.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        base.where(APP_ROLE.SLUG.in(allowedRoleSlugs));

        // Count total items
        Long totalItemsNullable = dsl.selectCount()
                .from(base.asTable("count_sub"))
                .fetchOne(0, long.class);

        long totalItems = totalItemsNullable == null ? 0 : totalItemsNullable;

        // Sorting
        List<SortRequest> sorts = parseSorts(paginationRequest.sort());
        sorts.forEach(sort -> base.orderBy(orderField(sort)));

        // Pagination
        int offset = paginationRequest.page() * paginationRequest.size();
        base.offset(offset).limit(paginationRequest.size());

        // Ejecutar
        var result = base.fetch();

        List<UserPreviewResponse> items = result.stream()
                .map(r -> {

                    String id = r.get(APP_USER.ID).toString();
                    String name = formatInvertedName(r.get(APP_USER.FIRST_NAME), r.get(APP_USER.LAST_NAME));
                    String email = r.get(APP_USER.EMAIL);
                    String phone = r.get(APP_USER.PHONE_NUMBER);

                    RoleResponse role = new RoleResponse(
                            r.get("role_id").toString(),
                            r.get("role_name", String.class),
                            r.get("role_slug", String.class)
                    );

                    String registrationDate = dateTimeFormatter.format(r.get(APP_USER.REGISTRATION_DATE));

                    // TODO: reemplazar con lógica real de books y loans
                    String borrowedBooks = "5";

                    String profilePictureUrl = profilePictureService.profilePictureUrl(r.get(APP_USER.PROFILE_PICTURE_URL));

                    Set<String> permissions = permissionsFor(currentUser, id, r.get("role_slug", String.class));

                    return UserPreviewResponse.builder()
                            .id(id)
                            .name(name)
                            .email(email)
                            .phone(phone)
                            .role(role)
                            .registrationDate(registrationDate)
                            .activeLoans(borrowedBooks)
                            .profilePictureUrl(profilePictureUrl)
                            .permissions(permissions)
                            .build();
                })
                .toList();


        long totalPages = (long) Math.ceil((double) totalItems / paginationRequest.size());

        return PaginationResponse.<UserPreviewResponse>builder()
                .items(items)
                .page(paginationRequest.page())
                .size(paginationRequest.size())
                .totalItems(totalItems)
                .totalPages((int) totalPages)
                .hasPrevious(paginationRequest.page() > 0)
                .hasNext(paginationRequest.page() < totalPages - 1)
                .build();
    }

    private Set<String> permissionsFor(CustomUserDetails currentUser, String id, String role) {
        Optional<RoleName> roleNameOptional = RoleName.parse(role);
        if (roleNameOptional.isEmpty()) {
            return new HashSet<>();
        }
        RoleName roleName = roleNameOptional.get();
        return userAuthorization.permissionsFor(currentUser, id, roleName);
    }

    private List<SortRequest> parseSorts(List<String> sorts) {
        if (CollectionUtils.isEmpty(sorts)) {
            return defaultSorts();
        }
        return sorts.stream()
                .map(SortRequest::parse)
                .toList();
    }

    private List<SortRequest> defaultSorts() {
        return DEFAULT_SORTS;
    }

    private SortField<?> orderField(SortRequest sortRequest) {
        Field<?> field = switch (sortRequest.sort()) {
            case "firstName" -> APP_USER.FIRST_NAME;
            case "lastName" -> APP_USER.LAST_NAME;
            case "email" -> APP_USER.EMAIL;
            case "phoneNumber" -> APP_USER.PHONE_NUMBER;
            case "registrationDate" -> APP_USER.REGISTRATION_DATE;
            case "role" -> APP_ROLE.NAME;
            case "activeLoans" -> APP_USER.LAST_NAME; // TODO add real logic
            default -> throw new IllegalArgumentException("Invalid sort: %s".formatted(sortRequest.sort()));
        };
        return SortingOrder.DESC.equals(sortRequest.order()) ? field.desc() : field.asc();
    }

    private String formatInvertedName(String firstName, String lastName) {
        return "%s, %s".formatted(lastName, firstName);
    }

    private DateTimeFormatter createDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(
                "dd/MMM/yyyy",
                new Locale.Builder().setLanguage("es").setRegion("MX").build()
        );
    }

}
