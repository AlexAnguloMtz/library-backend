package com.unison.practicas.desarrollo.library.service.book;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.entity.book.Author;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.AuthorRepository;
import lombok.Builder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class ExportAuthors {

    private final AuthorRepository authorRepository;
    private final TemplateEngine templateEngine;
    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter dateTimeFormatter;

    public ExportAuthors(TemplateEngine templateEngine, AuthorRepository authorRepository) {
        this.templateEngine = templateEngine;
        this.authorRepository = authorRepository;
        this.dateFormatter = createDateFormatter();
        this.dateTimeFormatter = createDateTimeFormatter();
    }

    public ExportResponse handle(CustomUserDetails currentUser, ExportRequest request) {
        if (!"pdf".equalsIgnoreCase(request.format())) {
            throw new IllegalArgumentException("Invalid format: %s. Allowed formats: [PDF]".formatted(request.format()));
        }

        List<Integer> ids = request.ids().stream()
                .map(Integer::parseInt)
                .toList();

        List<Author> authors = authorRepository.findAllById(ids);

        return ExportResponse.builder()
                .fileBytes(generatePdf(currentUser, authors))
                .fileName("authors_export.pdf")
                .mediaType(MediaType.APPLICATION_PDF)
                .build();
    }

    private byte[] generatePdf(CustomUserDetails currentUser, List<Author> authors) {
        User user = currentUser.getUser();

        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {

            var context = new Context();
            context.setVariable("currentUser", user);
            context.setVariable("authors", authors.stream().map(this::toExportModel).toList());
            context.setVariable("exportDate", dateTimeFormatter.format(Instant.now().atOffset(ZoneOffset.UTC)));

            String html = templateEngine.process("exports/authors", context);

            var builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(byteArrayOutputStream);
            builder.run();

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }

    private ExportAuthors.AuthorExportModel toExportModel(Author author) {
        return AuthorExportModel.builder()
                .id(author.getId().toString())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .dateOfBirth(dateFormatter.format(author.getDateOfBirth()))
                .country(author.getCountry().getNicename())
                .bookCount(String.valueOf(author.getBooks().size()))
                .build();
    }

    @Builder
    private record AuthorExportModel(
            String id,
            String firstName,
            String lastName,
            String dateOfBirth,
            String country,
            String bookCount
    ) {}

    private DateTimeFormatter createDateFormatter() {
        return DateTimeFormatter.ofPattern(
                "dd/MMM/yyyy",
                new Locale.Builder()
                        .setLanguage("es")
                        .setRegion("MX")
                        .build()
        );
    }

    private DateTimeFormatter createDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(
                "dd/MMM/yyyy HH:mm:ss",
                new Locale.Builder()
                        .setLanguage("es")
                        .setRegion("MX")
                        .build()
        );
    }


}
