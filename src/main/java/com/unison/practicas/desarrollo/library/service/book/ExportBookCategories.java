package com.unison.practicas.desarrollo.library.service.book;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.entity.book.BookCategory;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.BookCategoryRepository;
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
public class ExportBookCategories {

    private final BookCategoryRepository bookCategoryRepository;
    private final TemplateEngine templateEngine;
    private final DateTimeFormatter dateTimeFormatter;

    public ExportBookCategories(TemplateEngine templateEngine, BookCategoryRepository bookCategoryRepository) {
        this.templateEngine = templateEngine;
        this.bookCategoryRepository = bookCategoryRepository;
        this.dateTimeFormatter = createDateTimeFormatter();
    }

    public ExportResponse handle(CustomUserDetails currentUser, ExportRequest request) {
        if (!"pdf".equalsIgnoreCase(request.format())) {
            throw new IllegalArgumentException("Invalid format: %s. Allowed formats: [PDF]".formatted(request.format()));
        }

        List<Integer> ids = request.ids().stream()
                .map(Integer::parseInt)
                .toList();

        List<BookCategory> bookCategories = bookCategoryRepository.findAllById(ids);

        return ExportResponse.builder()
                .fileBytes(generatePdf(currentUser, bookCategories))
                .fileName("book_categories_export.pdf")
                .mediaType(MediaType.APPLICATION_PDF)
                .build();
    }

    private byte[] generatePdf(CustomUserDetails currentUser, List<BookCategory> bookCategories) {
        User user = currentUser.getUser();

        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {

            var context = new Context();
            context.setVariable("currentUser", user);
            context.setVariable("bookCategories", bookCategories.stream().map(this::toExportModel).toList());
            context.setVariable("exportDate", dateTimeFormatter.format(Instant.now().atOffset(ZoneOffset.UTC)));

            String html = templateEngine.process("exports/book-categories", context);

            var builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(byteArrayOutputStream);
            builder.run();

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }

    private ExportBookCategories.BookCategoryExportModel toExportModel(BookCategory bookCategory) {
        return BookCategoryExportModel.builder()
                .id(bookCategory.getId().toString())
                .name(bookCategory.getName())
                .bookCount(String.valueOf(bookCategory.getBooks() != null ? bookCategory.getBooks().size() : 0))
                .build();
    }

    @Builder
    private record BookCategoryExportModel(
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
