package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.response.UsersAcquisitionResponse;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.unison.practicas.desarrollo.library.jooq.Tables.APP_USER;
import static org.jooq.impl.DSL.count;

@Component
public class GetUsersAcquisition {

    private final DSLContext dsl;

    public GetUsersAcquisition(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<UsersAcquisitionResponse> get() {
        // 1️⃣ Traemos todos los meses con registros de usuarios
        List<Record3<Integer, Integer, Integer>> records = dsl
                .select(
                        org.jooq.impl.DSL.year(APP_USER.REGISTRATION_DATE).as("year"),
                        org.jooq.impl.DSL.month(APP_USER.REGISTRATION_DATE).as("month"),
                        count(APP_USER.ID).as("new_users") // usa count(field) moderno
                )
                .from(APP_USER)
                .groupBy(
                        org.jooq.impl.DSL.year(APP_USER.REGISTRATION_DATE),
                        org.jooq.impl.DSL.month(APP_USER.REGISTRATION_DATE)
                )
                .orderBy(
                        org.jooq.impl.DSL.year(APP_USER.REGISTRATION_DATE),
                        org.jooq.impl.DSL.month(APP_USER.REGISTRATION_DATE)
                )
                .fetch();

        // 2️⃣ Convertimos a lista temporal de year+month -> newUsers
        List<MonthData> temp = new ArrayList<>();
        for (Record3<Integer, Integer, Integer> r : records) {
            temp.add(new MonthData(
                    r.get("year", Integer.class),
                    r.get("month", Integer.class),
                    r.get("new_users", Integer.class)
            ));
        }

        if (temp.isEmpty()) return new ArrayList<>();

        // 3️⃣ Determinar rango completo de meses (inclusivo)
        int minYear = temp.stream().mapToInt(t -> t.year).min().orElse(temp.get(0).year);
        int maxYear = temp.stream().mapToInt(t -> t.year).max().orElse(temp.get(temp.size() - 1).year);

        YearMonth start = YearMonth.of(minYear, 1);
        YearMonth end = YearMonth.of(maxYear, 12);

        List<UsersAcquisitionResponse> result = new ArrayList<>();
        int cumulative = 0;

        // 4️⃣ Iteramos mes a mes con for clásico
        for (int year = start.getYear(); year <= end.getYear(); year++) {
            int theYear = year;
            int monthStart = (year == start.getYear()) ? start.getMonthValue() : 1;
            int monthEnd = (year == end.getYear()) ? end.getMonthValue() : 12;

            for (int month = monthStart; month <= monthEnd; month++) {
                int theMonth = month;
                MonthData existing = temp.stream()
                        .filter(t -> t.year == theYear && t.month == theMonth)
                        .findFirst()
                        .orElse(null);

                int newUsers = existing != null ? existing.newUsers : 0;
                int usersAtBeginning = cumulative;
                cumulative += newUsers;
                int usersAtEnd = cumulative;

                String monthName = YearMonth.of(year, month)
                        .getMonth()
                        .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

                result.add(UsersAcquisitionResponse.builder()
                        .year(year)
                        .month(monthName)
                        .usersAtBeginning(usersAtBeginning)
                        .usersAtEnd(usersAtEnd)
                        .newUsers(newUsers)
                        .build());
            }
        }

        return result;
    }

    // Clase auxiliar temporal
    private static class MonthData {
        int year;
        int month;
        int newUsers;

        MonthData(int year, int month, int newUsers) {
            this.year = year;
            this.month = month;
            this.newUsers = newUsers;
        }
    }
}
