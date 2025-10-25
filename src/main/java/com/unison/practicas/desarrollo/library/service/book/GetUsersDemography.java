package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.response.UsersDemographyResponse;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.unison.practicas.desarrollo.library.jooq.tables.AppUser.APP_USER;
import static com.unison.practicas.desarrollo.library.jooq.tables.Gender.GENDER;

@Component
public class GetUsersDemography {

    private final DSLContext dsl;

    public GetUsersDemography(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<UsersDemographyResponse> get() {

        var ageRanges = DSL.select(DSL.val(0).as("age_min"), DSL.val(9).as("age_max"))
                .unionAll(DSL.select(DSL.val(10), DSL.val(19)))
                .unionAll(DSL.select(DSL.val(20), DSL.val(29)))
                .unionAll(DSL.select(DSL.val(30), DSL.val(39)))
                .unionAll(DSL.select(DSL.val(40), DSL.val(49)))
                .unionAll(DSL.select(DSL.val(50), DSL.val(59)))
                .unionAll(DSL.select(DSL.val(60), DSL.val(69)))
                .unionAll(DSL.select(DSL.val(70), DSL.val(79)))
                .unionAll(DSL.select(DSL.val(80), DSL.val(89)))
                .unionAll(DSL.select(DSL.val(90), DSL.val(99)))
                .asTable("age_ranges");

        // Calcula la edad con SQL nativo: EXTRACT(YEAR FROM age(current_date, date_of_birth))
        var ageExpression = DSL.field("EXTRACT(YEAR FROM AGE(CURRENT_DATE, {0}))", Integer.class, APP_USER.DATE_OF_BIRTH);

        return dsl
                .select(
                        GENDER.NAME.as("gender"),
                        ageRanges.field("age_min", Integer.class),
                        ageRanges.field("age_max", Integer.class),
                        DSL.count(APP_USER.ID).as("frequency")
                )
                .from(GENDER)
                .crossJoin(ageRanges)
                .leftJoin(APP_USER)
                .on(APP_USER.GENDER_ID.eq(GENDER.ID))
                .and(ageExpression.between(
                        ageRanges.field("age_min", Integer.class),
                        ageRanges.field("age_max", Integer.class)
                ))
                .groupBy(GENDER.NAME, ageRanges.field("age_min"), ageRanges.field("age_max"))
                .orderBy(GENDER.NAME, ageRanges.field("age_min"))
                .fetchInto(UsersDemographyResponse.class);
    }
}
