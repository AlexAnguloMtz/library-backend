package com.unison.practicas.desarrollo.library.service.book;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.entity.book.Publisher;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.PublisherRepository;
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
public class ExportPublishers {

    private final PublisherRepository publisherRepository;
    private final TemplateEngine templateEngine;
    private final DateTimeFormatter dateTimeFormatter;

    public ExportPublishers(TemplateEngine templateEngine, PublisherRepository publisherRepository) {
        this.templateEngine = templateEngine;
        this.publisherRepository = publisherRepository;
        this.dateTimeFormatter = createDateTimeFormatter();
    }

    public ExportResponse handle(CustomUserDetails currentUser, ExportRequest request) {
        if (!"pdf".equalsIgnoreCase(request.format())) {
            throw new IllegalArgumentException("Invalid format: %s. Allowed formats: [PDF]".formatted(request.format()));
        }

        List<Integer> ids = request.ids().stream()
                .map(Integer::parseInt)
                .toList();

        List<Publisher> publishers = publisherRepository.findAllById(ids);

        return ExportResponse.builder()
                .fileBytes(generatePdf(currentUser, publishers))
                .fileName("publishers_export.pdf")
                .mediaType(MediaType.APPLICATION_PDF)
                .build();
    }

    private byte[] generatePdf(CustomUserDetails currentUser, List<Publisher> publishers) {
        User user = currentUser.getUser();

        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {

            var context = new Context();
            context.setVariable("currentUser", user);
            context.setVariable("publishers", publishers.stream().map(this::toExportModel).toList());
            context.setVariable("exportDate", dateTimeFormatter.format(Instant.now().atOffset(ZoneOffset.UTC)));

            String html = templateEngine.process("exports/publishers", context);

            var builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(byteArrayOutputStream);
            builder.run();

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }

    private PublisherExportModel toExportModel(Publisher publisher) {
        return PublisherExportModel.builder()
                .id(publisher.getId().toString())
                .name(publisher.getName())
                .bookCount(String.valueOf(publisher.getBooks() != null ? publisher.getBooks().size() : 0))
                .build();
    }

    @Builder
    private record PublisherExportModel(
            String id,
            String name,
            String bookCount
    ) {}

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

