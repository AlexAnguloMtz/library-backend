package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.response.BookCategoryPopularityResponse;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.unison.practicas.desarrollo.library.jooq.tables.AppUser.APP_USER;
import static com.unison.practicas.desarrollo.library.jooq.tables.Book.BOOK;
import static com.unison.practicas.desarrollo.library.jooq.tables.BookCopy.BOOK_COPY;
import static com.unison.practicas.desarrollo.library.jooq.tables.BookCategory.BOOK_CATEGORY;
import static com.unison.practicas.desarrollo.library.jooq.tables.BookLoan.BOOK_LOAN;
import static com.unison.practicas.desarrollo.library.jooq.tables.Gender.GENDER;

@Component
public class GetBookCategoriesPopularity {

    private final DSLContext dsl;

    public GetBookCategoriesPopularity(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<BookCategoryPopularityResponse> get() {

        var loansPerUser = DSL.select(
                        BOOK_CATEGORY.NAME.as("category"),
                        DSL.case_()
                                .when(GENDER.NAME.eq("Masculino"), "Hombres")
                                .when(GENDER.NAME.eq("Femenino"), "Mujeres")
                                .otherwise(GENDER.NAME)
                                .as("gender"),
                        DSL.floor(
                                DSL.field("EXTRACT(YEAR FROM AGE(current_date, {0}))", Integer.class, APP_USER.DATE_OF_BIRTH)
                                        .div(10)
                        ).mul(10).as("age_min"),
                        DSL.floor(
                                DSL.field("EXTRACT(YEAR FROM AGE(current_date, {0}))", Integer.class, APP_USER.DATE_OF_BIRTH)
                                        .div(10)
                        ).mul(10).add(9).as("age_max"),
                        BOOK_LOAN.USER_ID,
                        DSL.count().as("loans_per_user")
                )
                .from(BOOK_LOAN)
                .join(BOOK_COPY).on(BOOK_COPY.ID.eq(BOOK_LOAN.BOOK_COPY_ID))
                .join(BOOK).on(BOOK.ID.eq(BOOK_COPY.BOOK_ID))
                .join(BOOK_CATEGORY).on(BOOK_CATEGORY.ID.eq(BOOK.CATEGORY_ID))
                .join(APP_USER).on(APP_USER.ID.eq(BOOK_LOAN.USER_ID))
                .join(GENDER).on(GENDER.ID.eq(APP_USER.GENDER_ID))
                .groupBy(BOOK_CATEGORY.NAME, GENDER.NAME, APP_USER.DATE_OF_BIRTH, BOOK_LOAN.USER_ID)
                .asTable("loans_per_user");

        var avgPerGroup = DSL.select(
                        loansPerUser.field("category"),
                        loansPerUser.field("gender"),
                        loansPerUser.field("age_min"),
                        loansPerUser.field("age_max"),
                        DSL.avg(loansPerUser.field("loans_per_user", Integer.class))
                                .as("value")
                )
                .from(loansPerUser)
                .groupBy(
                        loansPerUser.field("category"),
                        loansPerUser.field("gender"),
                        loansPerUser.field("age_min"),
                        loansPerUser.field("age_max")
                )
                .asTable("avg_per_group");

        var rankedTop = DSL.select(
                        avgPerGroup.field("category"),
                        avgPerGroup.field("gender"),
                        avgPerGroup.field("age_min"),
                        avgPerGroup.field("age_max"),
                        avgPerGroup.field("value"),
                        DSL.rowNumber()
                                .over(DSL.partitionBy(
                                        avgPerGroup.field("gender"),
                                        avgPerGroup.field("age_min"),
                                        avgPerGroup.field("age_max")
                                ).orderBy(avgPerGroup.field("value").desc()))
                                .as("rn")
                )
                .from(avgPerGroup)
                .asTable("ranked_top");

        return dsl.select(
                        rankedTop.field("category", String.class),
                        rankedTop.field("gender", String.class),
                        rankedTop.field("age_min", Integer.class),
                        rankedTop.field("age_max", Integer.class),
                        rankedTop.field("value", Double.class)
                )
                .from(rankedTop)
                .where(DSL.field("rn").le(5))
                .fetch()
                .map(record -> {
                    double value = record.get("value", Double.class);
                    value = Math.round(value * 100.0) / 100.0; // round for 2 decimals
                    return BookCategoryPopularityResponse.builder()
                            .category(record.get("category", String.class))
                            .gender(record.get("gender", String.class))
                            .ageMin(record.get("age_min", Integer.class))
                            .ageMax(record.get("age_max", Integer.class))
                            .value(value)
                            .build();
                });
    }
}
