package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.response.LoansDistributionResponse;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.unison.practicas.desarrollo.library.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

@Component
public class GetLoansDistribution {

    private final DSLContext dsl;

    public GetLoansDistribution(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<LoansDistributionResponse> get() {
        Field<Integer> yearField = year(BOOK_LOAN.LOAN_DATE).as("year");
        Field<Integer> monthField = month(BOOK_LOAN.LOAN_DATE).as("month");

        Field<String> genderField = case_()
                .when(GENDER.NAME.eq("Masculino"), inline("Hombres"))
                .when(GENDER.NAME.eq("Femenino"), inline("Mujeres"))
                .otherwise(GENDER.NAME)
                .as("gender");

        var loanCounts = dsl
                .select(
                        yearField,
                        monthField,
                        genderField,
                        count().as("loan_count")
                )
                .from(BOOK_LOAN)
                .join(APP_USER).on(APP_USER.ID.eq(BOOK_LOAN.USER_ID))
                .join(GENDER).on(GENDER.ID.eq(APP_USER.GENDER_ID))
                .groupBy(yearField, monthField, genderField)
                .asTable("loan_counts");

        var genders = dsl
                .select(field("gender", String.class))
                .from(values(
                        row(inline("Hombres")),
                        row(inline("Mujeres"))
                ).as("fixed_genders", "gender"))
                .unionAll(
                        dsl.select(GENDER.NAME)
                                .from(GENDER)
                                .where(GENDER.NAME.notIn("Masculino", "Femenino"))
                )
                .asTable("genders");

        var months = dsl
                .select(yearField, monthField)
                .from(BOOK_LOAN)
                .groupBy(yearField, monthField)
                .asTable("months");

        return dsl
                .select(
                        field(name("months", "year"), Integer.class).as("year"),
                        field(name("months", "month"), Integer.class).as("month"),
                        field(name("genders", "gender"), String.class).as("gender"),
                        coalesce(field(name("loan_counts", "loan_count"), Integer.class), inline(0)).as("loan_count")
                )
                .from(months)
                .crossJoin(genders)
                .leftJoin(loanCounts)
                .on(
                        field(name("months", "year"), Integer.class).eq(field(name("loan_counts", "year"), Integer.class))
                                .and(field(name("months", "month"), Integer.class).eq(field(name("loan_counts", "month"), Integer.class)))
                                .and(field(name("genders", "gender"), String.class).eq(field(name("loan_counts", "gender"), String.class)))
                )
                .orderBy(field("year"), field("month"), field("gender"))
                .fetch(record -> LoansDistributionResponse.builder()
                        .year(record.get("year", Integer.class))
                        .month(record.get("month", Integer.class))
                        .gender(record.get("gender", String.class))
                        .value(record.get("loan_count", Integer.class))
                        .build()
                );
    }
}
