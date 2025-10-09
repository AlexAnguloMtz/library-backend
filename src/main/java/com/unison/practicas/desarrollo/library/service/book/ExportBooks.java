package com.unison.practicas.desarrollo.library.service.book;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.entity.book.Book;
import com.unison.practicas.desarrollo.library.entity.book.Author;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.BookRepository;
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
public class ExportBooks {

    private final BookRepository bookRepository;
    private final TemplateEngine templateEngine;
    private final DateTimeFormatter dateTimeFormatter;

    public ExportBooks(BookRepository bookRepository, TemplateEngine templateEngine) {
        this.bookRepository = bookRepository;
        this.templateEngine = templateEngine;
        this.dateTimeFormatter = createDateTimeFormatter();
    }

    public ExportResponse handle(CustomUserDetails currentUser, ExportRequest request) {
        if (!"pdf".equalsIgnoreCase(request.format())) {
            throw new IllegalArgumentException("Invalid format: %s. Allowed formats: [PDF]".formatted(request.format()));
        }

        List<Integer> ids = request.ids().stream()
                .map(Integer::parseInt)
                .toList();

        List<Book> books = bookRepository.findAllById(ids);

        return ExportResponse.builder()
                .fileBytes(generatePdf(currentUser, books))
                .fileName("books_export.pdf")
                .mediaType(MediaType.APPLICATION_PDF)
                .build();
    }

    private byte[] generatePdf(CustomUserDetails currentUser, List<Book> books) {
        User user = currentUser.getUser();

        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {

            var context = new Context();
            context.setVariable("currentUser", user);
            context.setVariable("books", books.stream().map(this::toExportModel).toList());
            context.setVariable("exportDate", dateTimeFormatter.format(Instant.now().atOffset(ZoneOffset.UTC)));

            String html = templateEngine.process("exports/books", context);

            var builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(byteArrayOutputStream);
            builder.run();

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }

    private ExportBooks.BookExportModel toExportModel(Book book) {
        return BookExportModel.builder()
                .id(book.getId().toString())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .category(book.getCategory().getName())
                .publisher(book.getPublisher().getName())
                .year(book.getYear().toString())
                .authors(book.getAuthors().stream().map(Author::getReversedFullName).toList())
                .build();
    }

    @Builder
    private record BookExportModel(
            String id,
            String title,
            String isbn,
            String category,
            String publisher,
            String year,
            List<String> authors
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
