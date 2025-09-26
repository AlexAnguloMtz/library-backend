package com.unison.practicas.desarrollo.library.service.user;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.entity.User;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ExportUsers {

    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;
    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter dateTimeFormatter;
    private final UserAuthorization userAuthorization;

    public ExportUsers(UserRepository userRepository, TemplateEngine templateEngine, UserAuthorization userAuthorization) {
        this.userRepository = userRepository;
        this.templateEngine = templateEngine;
        this.userAuthorization = userAuthorization;
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

        List<User> users = userRepository.findAllById(ids);

        users.forEach(user -> {
            if (!userAuthorization.canReadUser(currentUser, user)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You don't have permissions to read at least one of the requested items");
            }
        });

        return ExportResponse.builder()
                .fileBytes(generatePdf(currentUser, users))
                .fileName("users_export.pdf")
                .mediaType(MediaType.APPLICATION_PDF)
                .build();
    }

    private byte[] generatePdf(CustomUserDetails currentUser, List<User> users) {
        User user = currentUser.getUser();

        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {

            var context = new Context();
            context.setVariable("currentUser", user);
            context.setVariable("users", users.stream().map(this::toExportModel).toList());
            context.setVariable("exportDate", dateTimeFormatter.format(Instant.now().atOffset(ZoneOffset.UTC)));

            String html = templateEngine.process("exports/users", context);

            var builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(byteArrayOutputStream);
            builder.run();

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }

    private UserExportModel toExportModel(User user) {
        return UserExportModel.builder()
                .id(user.getId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .registrationDate(dateFormatter.format(Instant.now().atOffset(ZoneOffset.UTC)))
                .role(user.getRole().getName())
                .gender(user.getGender().getName())
                .build();
    }

    @Builder
    private record UserExportModel(
            String id,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String registrationDate,
            String gender,
            String role
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