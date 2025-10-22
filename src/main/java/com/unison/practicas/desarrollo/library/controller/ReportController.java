package com.unison.practicas.desarrollo.library.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.unison.practicas.desarrollo.library.entity.book.Book;
import com.unison.practicas.desarrollo.library.entity.book.BookCopy;
import com.unison.practicas.desarrollo.library.entity.book.BookLoan;

import com.unison.practicas.desarrollo.library.repository.BookLoanRepository;
import com.unison.practicas.desarrollo.library.repository.BookRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;

    public ReportController(BookRepository bookRepository, BookLoanRepository bookLoanRepository) {
        this.bookRepository = bookRepository;
        this.bookLoanRepository = bookLoanRepository;
    }

    @GetMapping("/books")
    public ResponseEntity<byte[]> generateBooksReport() {
        try {
            // === Preparar flujo de salida PDF ===
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // === Encabezado del reporte ===
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("📚 Reporte General de Libros", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.systemDefault());

            document.add(new Paragraph("Generado el: " + formatter.format(Instant.now())));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Detalles de los libros y sus préstamos registrados:"));
            document.add(new Paragraph(" "));

            // === Crear tabla de datos ===
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell("Título");
            table.addCell("Autor(es)");
            table.addCell("Fecha de Préstamo");
            table.addCell("Fecha de Vencimiento");
            table.addCell("Fecha de Devolución");

            // === Consultar libros desde la base de datos ===
            List<Book> books = bookRepository.findAll();

            for (Book book : books) {
                // Buscar préstamos por cada libro a través de sus copias
                List<BookLoan> loans = bookLoanRepository.findAll().stream()
                        .filter(l -> {
                            BookCopy copy = l.getBookCopy();
                            return copy != null && copy.getBook() != null
                                    && copy.getBook().getId().equals(book.getId());
                        })
                        .collect(Collectors.toList());

                if (loans.isEmpty()) {
                    // Libros sin préstamo
                    table.addCell(book.getTitle());
                    table.addCell(
                            book.getAuthors().stream()
                                    .map(a -> a.getFullName()) // ✅ Autor corregido
                                    .collect(Collectors.joining(", "))
                    );
                    table.addCell("-");
                    table.addCell("-");
                    table.addCell("-");
                } else {
                    // Libros con préstamos registrados
                    for (BookLoan loan : loans) {
                        table.addCell(book.getTitle());
                        table.addCell(
                                book.getAuthors().stream()
                                        .map(a -> a.getFullName())
                                        .collect(Collectors.joining(", "))
                        );

                        table.addCell(
                                loan.getLoanDate() != null
                                        ? formatter.format(loan.getLoanDate())
                                        : "-"
                        );

                        table.addCell(
                                loan.getDueDate() != null
                                        ? formatter.format(loan.getDueDate())
                                        : "-"
                        );

                        Optional<Instant> returnDateOpt = loan.getReturnDate();
                        table.addCell(
                                returnDateOpt.isPresent()
                                        ? formatter.format(returnDateOpt.get())
                                        : "-"
                        );
                    }
                }
            }

            // === Agregar tabla y cerrar documento ===
            document.add(table);
            document.close();

            // === Retornar respuesta PDF descargable ===
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books-report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


}
