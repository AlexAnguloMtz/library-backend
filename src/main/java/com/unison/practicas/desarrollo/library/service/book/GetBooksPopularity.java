package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.PopularityRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.BookPopularityResponse;
import com.unison.practicas.desarrollo.library.util.PopularityMetric;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.unison.practicas.desarrollo.library.jooq.tables.AppUser.APP_USER;
import static com.unison.practicas.desarrollo.library.jooq.tables.Book.BOOK;
import static com.unison.practicas.desarrollo.library.jooq.tables.BookCopy.BOOK_COPY;
import static com.unison.practicas.desarrollo.library.jooq.tables.BookLoan.BOOK_LOAN;
import static com.unison.practicas.desarrollo.library.jooq.tables.Gender.GENDER;

@Component
public class GetBooksPopularity {

    private final DSLContext dsl;
    private final BookImageService bookImageService;

    public GetBooksPopularity(DSLContext dsl, BookImageService bookImageService) {
        this.dsl = dsl;
        this.bookImageService = bookImageService;
    }

    public List<BookPopularityResponse> get(PopularityRequest request) {

        PopularityMetric metric = parseMetric(request.metric());

        var loansPerUser = dsl.select(
                        BOOK.ID.as("book_id"),
                        BOOK.ISBN.as("book_isbn"),
                        BOOK.TITLE.as("book_title"),
                        BOOK.IMAGE.as("book_image"),
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
                .join(APP_USER).on(APP_USER.ID.eq(BOOK_LOAN.USER_ID))
                .join(GENDER).on(GENDER.ID.eq(APP_USER.GENDER_ID))
                .groupBy(BOOK.ID, BOOK.TITLE, BOOK.ISBN, BOOK.IMAGE, GENDER.NAME, APP_USER.DATE_OF_BIRTH, BOOK_LOAN.USER_ID)
                .asTable("loans_per_user");

        var valueField = aggregateFieldForMetric(metric, loansPerUser);

        var aggPerGroup = dsl.select(
                        loansPerUser.field("book_id", Integer.class),
                        loansPerUser.field("book_isbn", String.class),
                        loansPerUser.field("book_title", String.class),
                        loansPerUser.field("book_image", String.class),
                        loansPerUser.field("gender", String.class),
                        loansPerUser.field("age_min", Integer.class),
                        loansPerUser.field("age_max", Integer.class),
                        valueField.as("value")
                )
                .from(loansPerUser)
                .groupBy(
                        loansPerUser.field("book_id", Integer.class),
                        loansPerUser.field("book_isbn", String.class),
                        loansPerUser.field("book_title", String.class),
                        loansPerUser.field("book_image", String.class),
                        loansPerUser.field("gender", String.class),
                        loansPerUser.field("age_min", Integer.class),
                        loansPerUser.field("age_max", Integer.class)
                )
                .asTable("agg_per_group");

        var allBooks =
                dsl.select(
                    BOOK.ID.as("book_id"),
                    BOOK.ISBN.as("book_isbn"),
                    BOOK.TITLE.as("book_title"),
                    BOOK.IMAGE.as("book_image"))
                .from(BOOK)
                .asTable("all_books");

        var allGenders = dsl.select(
                        DSL.case_()
                                .when(GENDER.NAME.eq("Masculino"), "Hombres")
                                .when(GENDER.NAME.eq("Femenino"), "Mujeres")
                                .otherwise(GENDER.NAME)
                                .as("gender")
                )
                .from(GENDER)
                .asTable("all_genders");
        var allAgeGroups = dsl.select(DSL.val(0).as("age_min"), DSL.val(9).as("age_max"))
                .unionAll(dsl.select(DSL.val(10), DSL.val(19)))
                .unionAll(dsl.select(DSL.val(20), DSL.val(29)))
                .unionAll(dsl.select(DSL.val(30), DSL.val(39)))
                .unionAll(dsl.select(DSL.val(40), DSL.val(49)))
                .unionAll(dsl.select(DSL.val(50), DSL.val(59)))
                .unionAll(dsl.select(DSL.val(60), DSL.val(69)))
                .unionAll(dsl.select(DSL.val(70), DSL.val(79)))
                .unionAll(dsl.select(DSL.val(80), DSL.val(89)))
                .unionAll(dsl.select(DSL.val(90), DSL.val(99)))
                .asTable("all_age_groups");

        var allCombinations = dsl.select(
                        allBooks.field("book_id", Integer.class),
                        allBooks.field("book_isbn", String.class),
                        allBooks.field("book_title", String.class),
                        allBooks.field("book_image", String.class),
                        allGenders.field("gender", String.class),
                        allAgeGroups.field("age_min", Integer.class),
                        allAgeGroups.field("age_max", Integer.class)
                )
                .from(allBooks)
                .crossJoin(allGenders)
                .crossJoin(allAgeGroups)
                .asTable("all_combinations");

        var filledAgg = dsl.select(
                        allCombinations.field("book_id", Integer.class),
                        allCombinations.field("book_isbn", String.class),
                        allCombinations.field("book_title", String.class),
                        allCombinations.field("book_image", String.class),
                        allCombinations.field("gender", String.class),
                        allCombinations.field("age_min", Integer.class),
                        allCombinations.field("age_max", Integer.class),
                        DSL.coalesce(aggPerGroup.field("value", Double.class), 0.0).as("value")
                )
                .from(allCombinations)
                .leftJoin(aggPerGroup)
                .on(allCombinations.field("book_id", Integer.class).eq(aggPerGroup.field("book_id", Integer.class)))
                .and(allCombinations.field("book_isbn", String.class).eq(aggPerGroup.field("book_isbn", String.class)))
                .and(allCombinations.field("book_title", String.class).eq(aggPerGroup.field("book_title", String.class)))
                .and(allCombinations.field("book_image", String.class).eq(aggPerGroup.field("book_image", String.class)))
                .and(allCombinations.field("gender", String.class).eq(aggPerGroup.field("gender", String.class)))
                .and(allCombinations.field("age_min", Integer.class).eq(aggPerGroup.field("age_min", Integer.class)))
                .and(allCombinations.field("age_max", Integer.class).eq(aggPerGroup.field("age_max", Integer.class)))
                .asTable("filled_agg");

        var rankedTop = dsl.select(
                        filledAgg.field("book_id", Integer.class),
                        filledAgg.field("book_isbn", String.class),
                        filledAgg.field("book_title", String.class),
                        filledAgg.field("book_image", String.class),
                        filledAgg.field("gender", String.class),
                        filledAgg.field("age_min", Integer.class),
                        filledAgg.field("age_max", Integer.class),
                        filledAgg.field("value", Double.class),
                        DSL.rowNumber()
                                .over(DSL.partitionBy(
                                        filledAgg.field("gender", String.class),
                                        filledAgg.field("age_min", Integer.class),
                                        filledAgg.field("age_max", Integer.class)
                                ).orderBy(filledAgg.field("value", Double.class).desc()))
                                .as("rn")
                )
                .from(filledAgg)
                .asTable("ranked_top");

        var query = dsl.select(
                        rankedTop.field("book_id", Integer.class),
                        rankedTop.field("book_isbn", String.class),
                        rankedTop.field("book_title", String.class),
                        rankedTop.field("book_image", String.class),
                        rankedTop.field("gender", String.class),
                        rankedTop.field("age_min", Integer.class),
                        rankedTop.field("age_max", Integer.class),
                        rankedTop.field("value", Double.class)
                )
                .from(rankedTop);

        if (request.limit() != null) {
            query.where(rankedTop.field("rn", Integer.class).le(request.limit()));
        }

        return query.fetch()
                .map(record -> {
                    double value = record.get("value", Double.class);
                    value = Math.round(value * 100.0) / 100.0; // Round for two digits
                    return BookPopularityResponse.builder()
                            .bookId(record.get("book_id", Integer.class).toString())
                            .bookIsbn(record.get("book_isbn", String.class))
                            .bookTitle(record.get("book_title", String.class))
                            .bookImageUrl(bookImageService.bookImageUrl(record.get("book_image", String.class)))
                            .gender(record.get("gender", String.class))
                            .ageMin(record.get("age_min", Integer.class))
                            .ageMax(record.get("age_max", Integer.class))
                            .value(value)
                            .build();
                });
    }

    private Field<?> aggregateFieldForMetric(PopularityMetric metric, Table<?> loansPerUser) {
        var loansCount = loansPerUser.field("loans_per_user", Integer.class);
        var userId = loansPerUser.field("user_id", Integer.class);

        return switch (metric) {
            case AVERAGE -> DSL.avg(loansCount);
            case DISTINCT_USERS -> DSL.countDistinct(userId);
            case FREQUENCY -> DSL.sum(loansCount);
            case MEDIAN -> DSL.field("percentile_cont(0.5) within group (order by {0})", Double.class, loansCount);
            case MODE -> DSL.field(
                    "mode() within group (order by {0})", Integer.class, loansCount
            );
        };
    }

    private PopularityMetric parseMetric(String str) {
        try {
            return PopularityMetric.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid metric: " + str);
        }
    }

}
