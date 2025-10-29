package com.unison.practicas.desarrollo.library.service.audit;

import com.unison.practicas.desarrollo.library.entity.audit.AuditEventEntity;
import com.unison.practicas.desarrollo.library.util.JsonUtils;
import com.unison.practicas.desarrollo.library.util.event.BookCategoryUpdated;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

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
            default -> throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Can't format pretty data for event type %s".formatted(eventTypeId));
        };
    }

    private String format(BookCategoryUpdated data) {
        String categoryId = data.getCategoryId();
        BookCategoryUpdated.Fields oldValues = data.getOldValues();
        BookCategoryUpdated.Fields newValues = data.getNewValues();

        StringBuilder html = new StringBuilder();

        html.append("""
        <p style='font-size: 0.9em; margin-bottom: 12px;'>
            ID de categoría: <strong>""" + categoryId + "</strong>" +
        "</p>"
        );

        html.append("""
        <table style='border-collapse: collapse; width: 100%; font-size: 0.9em;'>
            <thead>
                <tr>
                    <th><strong>Dato</strong></th>
                    <th><strong>Antes</strong></th>
                    <th><strong>Después</strong></th>
                </tr>
            </thead>
            <tbody>
    """);

        html.append("<tr>")
                .append("<td>Nombre de categoría</td>")
                .append("<td>")
                .append(oldValues != null && oldValues.name() != null ? oldValues.name() : "")
                .append("</td>")
                .append("<td>")
                .append(newValues != null && newValues.name() != null ? newValues.name() : "")
                .append("</td>")
                .append("</tr>");

        html.append("</tbody></table>");

        return html.toString();
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
