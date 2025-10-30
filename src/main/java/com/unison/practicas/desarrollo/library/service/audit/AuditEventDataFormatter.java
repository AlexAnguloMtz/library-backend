package com.unison.practicas.desarrollo.library.service.audit;

import com.unison.practicas.desarrollo.library.entity.audit.AuditEventEntity;
import com.unison.practicas.desarrollo.library.util.JsonUtils;
import com.unison.practicas.desarrollo.library.util.event.*;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
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
            case
                    "BOOK_CATEGORY_CREATED",
                    "BOOK_CATEGORY_DELETED",
                    "AUTHOR_CREATED",
                    "AUTHOR_DELETED",
                    "PUBLISHER_CREATED",
                    "PUBLISHER_DELETED"
                    -> formatGeneric(eventTypeId, event.getEventData());

            case "BOOK_CATEGORY_UPDATED" -> format(jsonUtils.fromJson(event.getEventData(), BookCategoryUpdated.class));

            case "BOOK_CATEGORIES_MERGED" -> format(jsonUtils.fromJson(event.getEventData(), BookCategoriesMerged.class));

            case "AUTHOR_UPDATED" -> format(jsonUtils.fromJson(event.getEventData(), AuthorUpdated.class));

            case "PUBLISHER_UPDATED" -> format(jsonUtils.fromJson(event.getEventData(), PublisherUpdated.class));

            case "PUBLISHERS_MERGED" -> format(jsonUtils.fromJson(event.getEventData(), PublishersMerged.class));

            case "BOOK_CREATED" -> format(jsonUtils.fromJson(event.getEventData(), BookCreated.class));

            case "BOOK_DELETED" -> format(jsonUtils.fromJson(event.getEventData(), BookDeleted.class));

            case "BOOK_UPDATED" -> format(jsonUtils.fromJson(event.getEventData(), BookUpdated.class));

            default -> throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Can't format pretty data for event type %s".formatted(eventTypeId));
        };
    }

    private String format(BookCreated data) {
        if (data == null) return "";

        List<DivTag> authorsHtml = data.getAuthors().stream()
                .map(author -> div(
                        div(
                                span("ID Autor").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(author.id()).withStyle("font-weight: bold;")
                        ),
                        div(
                                span("Nombre").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(author.firstName()).withStyle("font-weight: bold;")
                        ),
                        div(
                                span("Apellido").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(author.lastName()).withStyle("font-weight: bold;")
                        )
                ).withStyle("margin-bottom: 10px;"))
                .toList();

        return html(
                body(
                        h4("Libro").withStyle("margin-bottom: 8px;"),
                        div(
                                div(
                                        span("ID Libro").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getBookId()).withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("ISBN").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getIsbn()).withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("Titulo").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getTitle()).withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("Año").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getYear() != null ? data.getYear().toString() : "").withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("ID Categoria").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getCategory() != null ? data.getCategory().id() : "").withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("Categoria").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getCategory() != null ? data.getCategory().name() : "").withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("ID Editorial").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getPublisher() != null ? data.getPublisher().id() : "").withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("Editorial").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getPublisher() != null ? data.getPublisher().name() : "").withStyle("font-weight: bold;")
                                )
                        ).withStyle("font-size: 0.9em; margin-bottom: 12px;"),

                        h4("Autores (" + (data.getAuthors() != null ? data.getAuthors().size() : 0) + ")")
                                .withStyle("margin-bottom: 8px;"),

                        each(authorsHtml, authorDiv -> authorDiv)
                )
        ).render();
    }

    private String format(BookDeleted data) {
        if (data == null) return "";

        List<DivTag> authorsHtml = data.getAuthors().stream()
                .map(author -> div(
                        div(
                                span("ID Autor").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(author.id()).withStyle("font-weight: bold;")
                        ),
                        div(
                                span("Nombre").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(author.firstName()).withStyle("font-weight: bold;")
                        ),
                        div(
                                span("Apellido").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(author.lastName()).withStyle("font-weight: bold;")
                        )
                ).withStyle("margin-bottom: 10px;"))
                .toList();

        return html(
                body(
                        h4("Libro").withStyle("margin-bottom: 8px;"),
                        div(
                                div(
                                        span("ID Libro").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getBookId()).withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("ISBN").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getIsbn()).withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("Titulo").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getTitle()).withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("Año").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getYear() != null ? data.getYear().toString() : "").withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("ID Categoria").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getCategory() != null ? data.getCategory().id() : "").withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("Categoria").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getCategory() != null ? data.getCategory().name() : "").withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("ID Editorial").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getPublisher() != null ? data.getPublisher().id() : "").withStyle("font-weight: bold;")
                                ),
                                div(
                                        span("Editorial").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                        strong(data.getPublisher() != null ? data.getPublisher().name() : "").withStyle("font-weight: bold;")
                                )
                        ).withStyle("font-size: 0.9em; margin-bottom: 12px;"),

                        h4("Autores (" + (data.getAuthors() != null ? data.getAuthors().size() : 0) + ")")
                                .withStyle("margin-bottom: 8px;"),

                        each(authorsHtml, authorDiv -> authorDiv)
                )
        ).render();
    }

    private String format(BookUpdated data) {
        if (data == null) return "";

        String id = data.getBookId();
        BookUpdated.Fields oldValues = data.getOldValues();
        BookUpdated.Fields newValues = data.getNewValues();

        List<DivTag> authorsBefore = oldValues != null && oldValues.authors() != null
                ? oldValues.authors().stream().map(a ->
                div(
                        div(
                                span("ID").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(a.id()).withStyle("font-weight: bold;")
                        ),
                        div(
                                span("Nombre").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(a.firstName()).withStyle("font-weight: bold;")
                        ),
                        div(
                                span("Apellido").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(a.lastName()).withStyle("font-weight: bold;")
                        )
                ).withStyle("font-size: 0.9em; margin-bottom: 6px;")
        ).toList() : List.of();

        List<DivTag> authorsAfter = newValues != null && newValues.authors() != null
                ? newValues.authors().stream().map(a ->
                div(
                        div(
                                span("ID").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(a.id()).withStyle("font-weight: bold;")
                        ),
                        div(
                                span("Nombre").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(a.firstName()).withStyle("font-weight: bold;")
                        ),
                        div(
                                span("Apellido").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(a.lastName()).withStyle("font-weight: bold;")
                        )
                ).withStyle("font-size: 0.9em; margin-bottom: 6px;")
        ).toList() : List.of();

        return html(
                body(
                        div(
                                span("ID Libro").withStyle("font-weight: lighter; width: 120px; display: inline-block;"),
                                strong(id).withStyle("font-weight: bold;")
                        ).withStyle("font-size: 0.9em; margin-bottom: 12px;"),

                        h3("Datos de Libro").withStyle("margin-bottom: 6px;"),

                        table(
                                thead(
                                        tr(
                                                th(strong("Campo")),
                                                th(strong("Antes")),
                                                th(strong("Después"))
                                        )
                                ),
                                tbody(
                                        tr(td("Título"),
                                                td(oldValues != null && oldValues.title() != null ? oldValues.title() : ""),
                                                td(newValues != null && newValues.title() != null ? newValues.title() : "")
                                        ),
                                        tr(td("ISBN"),
                                                td(oldValues != null && oldValues.isbn() != null ? oldValues.isbn() : ""),
                                                td(newValues != null && newValues.isbn() != null ? newValues.isbn() : "")
                                        ),
                                        tr(td("Año"),
                                                td(oldValues != null && oldValues.year() != null ? oldValues.year().toString() : ""),
                                                td(newValues != null && newValues.year() != null ? newValues.year().toString() : "")
                                        ),
                                        tr(td("ID Categoría"),
                                                td(oldValues != null && oldValues.category() != null ? oldValues.category().id() : ""),
                                                td(newValues != null && newValues.category() != null ? newValues.category().id() : "")
                                        ),
                                        tr(td("Categoría"),
                                                td(oldValues != null && oldValues.category() != null ? oldValues.category().name() : ""),
                                                td(newValues != null && newValues.category() != null ? newValues.category().name() : "")
                                        ),
                                        tr(td("ID Editorial"),
                                                td(oldValues != null && oldValues.publisher() != null ? oldValues.publisher().id() : ""),
                                                td(newValues != null && newValues.publisher() != null ? newValues.publisher().id() : "")
                                        ),
                                        tr(td("Editorial"),
                                                td(oldValues != null && oldValues.publisher() != null ? oldValues.publisher().name() : ""),
                                                td(newValues != null && newValues.publisher() != null ? newValues.publisher().name() : "")
                                        )
                                )
                        ).withStyle("border-collapse: collapse; width: 100%; font-size: 0.9em; margin-bottom: 12px;"),

                        h3("Autores antes del cambio (" + authorsBefore.size() + ")").withStyle("margin-bottom: 6px;"),
                        each(authorsBefore, a -> a),

                        h3("Autores después del cambio (" + authorsAfter.size() + ")").withStyle("margin-bottom: 6px;"),
                        each(authorsAfter, a -> a)
                )
        ).render();
    }

    private String format(PublisherUpdated data) {
        if (data == null) return "";

        String id = data.getPublisherId();
        PublisherUpdated.Fields oldValues = data.getOldValues();
        PublisherUpdated.Fields newValues = data.getNewValues();

        return p(
                text("ID de autor: "),
                strong(id)
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
                                        td("Nombre"),
                                        td(oldValues.name() != null ? oldValues.name() : ""),
                                        td(newValues.name() != null ? newValues.name() : "")
                                )
                        )
                )
                        .withStyle("border-collapse: collapse; width: 100%; font-size: 0.9em;")
                        .render();
    }

    private String format(AuthorUpdated data) {
        if (data == null) return "";

        String id = data.getAuthorId();
        AuthorUpdated.Fields oldValues = data.getOldValues();
        AuthorUpdated.Fields newValues = data.getNewValues();

        return p(
                text("ID de autor: "),
                strong(id)
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
                                        td("Nombre"),
                                        td(oldValues.firstName() != null ? oldValues.firstName() : ""),
                                        td(newValues.firstName() != null ? newValues.firstName() : "")
                                ),
                                tr(
                                        td("Apellido"),
                                        td(oldValues.lastName() != null ? oldValues.lastName() : ""),
                                        td(newValues.lastName() != null ? newValues.lastName() : "")
                                ),
                                tr(
                                        td("Nacionalidad"),
                                        td(oldValues.nationality() != null ? oldValues.nationality() : ""),
                                        td(newValues.nationality() != null ? newValues.nationality() : "")
                                ),
                                tr(
                                        td("Fecha de nacimiento"),
                                        td(oldValues.dateOfBirth() != null ? oldValues.dateOfBirth().toString() : ""),
                                        td(newValues.dateOfBirth() != null ? newValues.dateOfBirth().toString() : "")
                                )
                        )
                )
                        .withStyle("border-collapse: collapse; width: 100%; font-size: 0.9em;")
                        .render();
    }

    private String format(BookCategoryUpdated data) {
        if (data == null) return "";

        String id = data.getCategoryId();
        BookCategoryUpdated.Fields oldValues = data.getOldValues();
        BookCategoryUpdated.Fields newValues = data.getNewValues();

        return p(
                text("ID de categoría: "),
                strong(id)
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

        StringBuilder htmlBuilder = new StringBuilder();

        List<Map<String, Object>> sections = Arrays.asList(
                new HashMap<>() {{
                    put("title", "Categoría resultante");
                    put("items", target != null ? List.of(target) : Collections.emptyList());
                    put("emptyMessage", "N/A");
                }},
                new HashMap<>() {{
                    put("title", "Categorías eliminadas (" + (merged != null ? merged.size() : 0) + ")");
                    put("items", merged != null ? merged : Collections.emptyList());
                    put("emptyMessage", "No hay categorías eliminadas");
                }}
        );

        sections.forEach(section -> {
            htmlBuilder.append(b((String) section.get("title")).render());

            // This casting is safe, we just created the maps on this same method
            @SuppressWarnings("unchecked")
            List<BookCategoriesMerged.MergedBookCategory> items =
                    (List<BookCategoriesMerged.MergedBookCategory>) section.get("items");

            DomContent[] rows;
            if (items != null && !items.isEmpty()) {
                rows = items.stream()
                        .map(cat -> tr(
                                td(cat.categoryId() != null ? cat.categoryId() : ""),
                                td(cat.name() != null ? cat.name() : ""),
                                td(cat.booksBeforeMerge() != null ? cat.booksBeforeMerge().toString() : ""),
                                td(cat.booksAfterMerge() != null ? cat.booksAfterMerge().toString() : "")
                        ))
                        .toArray(DomContent[]::new);
            } else {
                rows = new DomContent[] { tr(td((String) section.get("emptyMessage")).attr("colspan", "4")) };
            }

            htmlBuilder.append(
                    table(
                            thead(
                                    tr(
                                            th(strong("ID")),
                                            th(strong("Nombre")),
                                            th(strong("Libros antes")),
                                            th(strong("Libros después"))
                                    )
                            ),
                            tbody(rows)
                    ).withStyle("border-collapse: collapse; width: 100%; font-size: 0.9em; margin-bottom: 16px;")
                            .render()
            );
        });

        return htmlBuilder.toString();
    }

    private String format(PublishersMerged data) {
        if (data == null) return "";

        var target = data.getTargetPublisher();
        var merged = data.getMergedPublishers();

        StringBuilder htmlBuilder = new StringBuilder();

        List<Map<String, Object>> sections = Arrays.asList(
                new HashMap<>() {{
                    put("title", "Editorial resultante");
                    put("items", target != null ? List.of(target) : Collections.emptyList());
                    put("emptyMessage", "N/A");
                }},
                new HashMap<>() {{
                    put("title", "Editoriales eliminadas (" + (merged != null ? merged.size() : 0) + ")");
                    put("items", merged != null ? merged : Collections.emptyList());
                    put("emptyMessage", "No hay editoriales eliminadas");
                }}
        );

        sections.forEach(section -> {
            htmlBuilder.append(b((String) section.get("title")).render());

            // This casting is safe, we just created the maps on this same method
            @SuppressWarnings("unchecked")
            List<PublishersMerged.MergedPublisher> items =
                    (List<PublishersMerged.MergedPublisher>) section.get("items");

            DomContent[] rows;
            if (items != null && !items.isEmpty()) {
                rows = items.stream()
                        .map(it -> tr(
                                td(it.publisherId() != null ? it.publisherId() : ""),
                                td(it.name() != null ? it.name() : ""),
                                td(it.booksBeforeMerge() != null ? it.booksBeforeMerge().toString() : ""),
                                td(it.booksAfterMerge() != null ? it.booksAfterMerge().toString() : "")
                        ))
                        .toArray(DomContent[]::new);
            } else {
                rows = new DomContent[] { tr(td((String) section.get("emptyMessage")).attr("colspan", "4")) };
            }

            htmlBuilder.append(
                    table(
                            thead(
                                    tr(
                                            th(strong("ID")),
                                            th(strong("Nombre")),
                                            th(strong("Libros antes")),
                                            th(strong("Libros después"))
                                    )
                            ),
                            tbody(rows)
                    ).withStyle("border-collapse: collapse; width: 100%; font-size: 0.9em; margin-bottom: 16px;")
                            .render()
            );
        });

        return htmlBuilder.toString();
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