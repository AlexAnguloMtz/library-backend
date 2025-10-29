package com.unison.practicas.desarrollo.library.service.audit;

import com.unison.practicas.desarrollo.library.entity.audit.AuditEventEntity;
import com.unison.practicas.desarrollo.library.util.JsonUtils;
import com.unison.practicas.desarrollo.library.util.event.BookCategoriesMerged;
import com.unison.practicas.desarrollo.library.util.event.BookCategoryUpdated;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import static j2html.TagCreator.*;

import java.util.*;

@Component
class AuditEventDataFormatter {

    private final JsonUtils jsonUtils;
    private final MessageSource auditMessageSource;

    AuditEventDataFormatter(
            JsonUtils jsonUtils,
            @Qualifier("auditMessageSource") MessageSource auditMessageSource
    ) {
        this.jsonUtils = jsonUtils;
        this.auditMessageSource = auditMessageSource;
    }

    String format(AuditEventEntity event) {
        String eventTypeId = event.getEventType().getId();
        return switch(eventTypeId) {
            case "BOOK_CATEGORY_CREATED", "BOOK_CATEGORY_DELETED" -> formatGeneric(eventTypeId, event.getEventData());
            case "BOOK_CATEGORY_UPDATED" -> format(jsonUtils.fromJson(event.getEventData(), BookCategoryUpdated.class));
            case "BOOK_CATEGORIES_MERGED" -> format(jsonUtils.fromJson(event.getEventData(), BookCategoriesMerged.class));
            default -> throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Can't format pretty data for event type %s".formatted(eventTypeId));
        };
    }

    private String format(BookCategoryUpdated data) {
        if (data == null) return "";

        String categoryId = data.getCategoryId();
        BookCategoryUpdated.Fields oldValues = data.getOldValues();
        BookCategoryUpdated.Fields newValues = data.getNewValues();

        return p(
                text("ID de categoría: "),
                strong(categoryId)
        )
                .withStyle("font-size: 0.9em; margin-bottom: 12px;")
                .render() +

                table(
                        thead(
                                tr(
                                        th(strong("Dato")),
                                        th(strong("Antes")),
                                        th(strong("Después"))
                                )
                        ),
                        tbody(
                                tr(
                                        td("Nombre de categoría"),
                                        td(oldValues != null && oldValues.name() != null ? oldValues.name() : ""),
                                        td(newValues != null && newValues.name() != null ? newValues.name() : "")
                                )
                        )
                )
                        .withStyle("border-collapse: collapse; width: 100%; font-size: 0.9em;")
                        .render();
    }

    private String format(BookCategoriesMerged data) {
        if (data == null) return "";

        var target = data.getTargetCategory();
        var merged = data.getMergedCategories();
        var booksMoved = data.getBooksMoved();

        String targetHtml = p(
                text("Categoría resultante:"),
                span(target != null ? target.name() : "N/A").withStyle("margin-left: 6px;")
        ).withStyle("font-size: 0.9em; margin-bottom: 12px;")
                .render();

        String booksHtml = p(
                text("Libros movidos:"),
                span(booksMoved != null ? booksMoved.toString() : "0").withStyle("margin-left: 6px;")
        ).withStyle("font-size: 0.9em; margin-bottom: 12px;")
                .render();

        String tableTitle = p(strong("Categorías eliminadas"))
                .withStyle("font-size: 0.9em; margin-bottom: 6px;")
                .render();

        String tableHtml = table(
                thead(
                        tr(
                                th(strong("ID de categoría")),
                                th(strong("Nombre de categoría"))
                        )
                ),
                tbody(
                        each(merged != null ? merged : List.of(), cat ->
                                tr(
                                        td(cat.categoryId()),
                                        td(cat.name())
                                )
                        )
                )
        ).withStyle("border-collapse: collapse; width: 100%; font-size: 0.9em;")
                .render();

        return targetHtml + booksHtml + tableTitle + tableHtml;
    }

    private String formatGeneric(String eventTypeId, String data) {
        Map<String, Object> values = jsonUtils.fromJson(data, Map.class);
        Map<String, Object> translatedValues = translateEventData(eventTypeId, values);
        List<Map.Entry<String, Object>> entries = new ArrayList<>(translatedValues.entrySet());
        Collections.reverse(entries);

        var html = new StringBuilder();
        html.append("<table style='border-collapse: collapse; width: 100%; font-size: 0.9em;'>");

        for (Map.Entry<String, Object> entry : entries) {
            html.append("<tr>")
                    .append("<th style='text-align: left; padding: 6px 8px; font-weight: 400;'>")
                    .append(entry.getKey())
                    .append("</th>")
                    .append("<td style='padding: 6px 8px; font-weight: 600;'>")
                    .append(entry.getValue() != null ? entry.getValue() : "")
                    .append("</td>")
                    .append("</tr>");
        }

        html.append("</table>");
        return html.toString();
    }


    private Map<String, Object> translateEventData(String eventTypeId, Map<String, Object> eventData) {
        Map<String, Object> translatedMap = new LinkedHashMap<>();

        Deque<StackFrame> stack = new ArrayDeque<>();
        stack.push(new StackFrame(eventData, translatedMap, null));

        while (!stack.isEmpty()) {
            StackFrame frame = stack.pop();

            for (Map.Entry<String, Object> entry : frame.original.entrySet()) {
                String fullKey = frame.parentKey == null ? entry.getKey() : frame.parentKey + "." + entry.getKey();
                String translatedKey = translate(eventTypeId + "." + fullKey);
                Object value = entry.getValue();

                if (value instanceof Map) {
                    Map<String, Object> newTranslated = new LinkedHashMap<>();
                    frame.translated.put(translatedKey, newTranslated);
                    stack.push(new StackFrame((Map<String, Object>) value, newTranslated, fullKey));
                } else {
                    frame.translated.put(translatedKey, value);
                }
            }
        }

        return translatedMap;
    }

    private static class StackFrame {
        Map<String, Object> original;
        Map<String, Object> translated;
        String parentKey;

        StackFrame(Map<String, Object> original, Map<String, Object> translated, String parentKey) {
            this.original = original;
            this.translated = translated;
            this.parentKey = parentKey;
        }

    }

    private String translate(String text) {
        return auditMessageSource.getMessage(text, null, text, null);
    }


}
