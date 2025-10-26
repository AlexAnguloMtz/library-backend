package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.AuthorsPopularityRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.AuthorPopularityResponse;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.unison.practicas.desarrollo.library.jooq.tables.AppUser.APP_USER;
import static com.unison.practicas.desarrollo.library.jooq.tables.Author.AUTHOR;
import static com.unison.practicas.desarrollo.library.jooq.tables.Book.BOOK;
import static com.unison.practicas.desarrollo.library.jooq.tables.BookAuthor.BOOK_AUTHOR;
import static com.unison.practicas.desarrollo.library.jooq.tables.BookCopy.BOOK_COPY;
import static com.unison.practicas.desarrollo.library.jooq.tables.BookLoan.BOOK_LOAN;
import static com.unison.practicas.desarrollo.library.jooq.tables.Gender.GENDER;

@Component
public class GetAuthorsPopularity {

    private final DSLContext dsl;

    public GetAuthorsPopularity(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<AuthorPopularityResponse> get(AuthorsPopularityRequest request) {

        Metric metric = parseMetric(request.metric());

        var loansPerUser = dsl.select(
                        AUTHOR.ID.as("author_id"),
                        AUTHOR.FIRST_NAME.as("author_first_name"),
                        AUTHOR.LAST_NAME.as("author_last_name"),
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
                .join(BOOK_AUTHOR).on(BOOK_AUTHOR.BOOK_ID.eq(BOOK.ID))
                .join(AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID))
                .join(APP_USER).on(APP_USER.ID.eq(BOOK_LOAN.USER_ID))
                .join(GENDER).on(GENDER.ID.eq(APP_USER.GENDER_ID))
                .groupBy(
                        AUTHOR.ID,
                        AUTHOR.FIRST_NAME,
                        AUTHOR.LAST_NAME,
                        GENDER.NAME,
                        APP_USER.DATE_OF_BIRTH,
                        BOOK_LOAN.USER_ID
                )
                .asTable("loans_per_user");

        var valueField = aggregateFieldForMetric(metric, loansPerUser);

        var aggPerGroup = dsl.select(
                        loansPerUser.field("author_id", Integer.class),
                        loansPerUser.field("author_first_name", String.class),
                        loansPerUser.field("author_last_name", String.class),
                        loansPerUser.field("gender", String.class),
                        loansPerUser.field("age_min", Integer.class),
                        loansPerUser.field("age_max", Integer.class),
                        valueField.as("value")
                )
                .from(loansPerUser)
                .groupBy(
                        loansPerUser.field("author_id"),
                        loansPerUser.field("author_first_name"),
                        loansPerUser.field("author_last_name"),
                        loansPerUser.field("gender"),
                        loansPerUser.field("age_min"),
                        loansPerUser.field("age_max")
                )
                .asTable("agg_per_group");

        var allAuthors = dsl.select(
                        AUTHOR.ID.as("author_id"),
                        AUTHOR.FIRST_NAME.as("author_first_name"),
                        AUTHOR.LAST_NAME.as("author_last_name")
                )
                .from(AUTHOR)
                .asTable("all_authors");

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
                        allAuthors.field("author_id", Integer.class),
                        allAuthors.field("author_first_name", String.class),
                        allAuthors.field("author_last_name", String.class),
                        allGenders.field("gender", String.class),
                        allAgeGroups.field("age_min", Integer.class),
                        allAgeGroups.field("age_max", Integer.class)
                )
                .from(allAuthors)
                .crossJoin(allGenders)
                .crossJoin(allAgeGroups)
                .asTable("all_combinations");

        var filledAgg = dsl.select(
                        allCombinations.field("author_id", Integer.class),
                        allCombinations.field("author_first_name", String.class),
                        allCombinations.field("author_last_name", String.class),
                        allCombinations.field("gender", String.class),
                        allCombinations.field("age_min", Integer.class),
                        allCombinations.field("age_max", Integer.class),
                        DSL.coalesce(aggPerGroup.field("value", Double.class), 0.0).as("value")
                )
                .from(allCombinations)
                .leftJoin(aggPerGroup)
                .on(allCombinations.field("author_id", Integer.class).eq(aggPerGroup.field("author_id", Integer.class)))
                .and(allCombinations.field("gender", String.class).eq(aggPerGroup.field("gender", String.class)))
                .and(allCombinations.field("age_min", Integer.class).eq(aggPerGroup.field("age_min", Integer.class)))
                .and(allCombinations.field("age_max", Integer.class).eq(aggPerGroup.field("age_max", Integer.class)))
                .asTable("filled_agg");

        var rankedTop = dsl.select(
                        filledAgg.field("author_id", Integer.class),
                        filledAgg.field("author_first_name", String.class),
                        filledAgg.field("author_last_name", String.class),
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
                        rankedTop.field("author_id", Integer.class),
                        rankedTop.field("author_first_name", String.class),
                        rankedTop.field("author_last_name", String.class),
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
                .map(record -> AuthorPopularityResponse.builder()
                        .authorId(String.valueOf(record.get("author_id", Integer.class)))
                        .authorFirstName(record.get("author_first_name", String.class))
                        .authorLastName(record.get("author_last_name", String.class))
                        .gender(record.get("gender", String.class))
                        .ageMin(record.get("age_min", Integer.class))
                        .ageMax(record.get("age_max", Integer.class))
                        .value(Math.round(record.get("value", Double.class) * 100.0) / 100.0)
                        .build());
    }

    private Field<?> aggregateFieldForMetric(Metric metric, Table<?> loansPerUser) {
        var loansCount = loansPerUser.field("loans_per_user", Integer.class);
        var userId = loansPerUser.field("user_id", Integer.class);

        return switch (metric) {
            case AVERAGE -> DSL.avg(loansCount);
            case DISTINCT_USERS -> DSL.countDistinct(userId);
            case FREQUENCY -> DSL.sum(loansCount);
            case MEDIAN -> DSL.field("percentile_cont(0.5) within group (order by {0})", Double.class, loansCount);
        };
    }

    private Metric parseMetric(String str) {
        try {
            return Metric.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid metric: " + str);
        }
    }

    private enum Metric {
        FREQUENCY,
        AVERAGE,
        MEDIAN,
        DISTINCT_USERS
    }
}