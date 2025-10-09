package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.book.request.GetPublishersRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.MergePublishersRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.PublisherRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.MergeBookCategoriesResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.MergePublishersResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.PublisherResponse;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.entity.book.BookCategory;
import com.unison.practicas.desarrollo.library.entity.book.Publisher;
import com.unison.practicas.desarrollo.library.repository.PublisherRepository;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final GetPublishers getPublishers;
    private final ExportPublishers exportPublishers;

    public PublisherService(GetPublishers getPublishers, PublisherRepository publisherRepository, ExportPublishers exportPublishers) {
        this.getPublishers = getPublishers;
        this.publisherRepository = publisherRepository;
        this.exportPublishers = exportPublishers;
    }

    @PreAuthorize("hasAuthority('publishers:read')")
    public PaginationResponse<PublisherResponse> getPublishers(
            GetPublishersRequest request,
            PaginationRequest pagination
    ) {
        return getPublishers.handle(request, pagination);
    }

    @PreAuthorize("hasAuthority('publishers:create')")
    @Transactional
    public PublisherResponse createPublisher(PublisherRequest request) {
        if (publisherRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with name '%s' already exists".formatted(request.name().trim()));
        }
        Publisher publisher = mapPublisher(request, new Publisher());
        return toResponse(publisherRepository.save(publisher));
    }

    @PreAuthorize("hasAuthority('publishers:update')")
    @Transactional
    public PublisherResponse updatePublisher(String id, PublisherRequest request) {
        Publisher publisherById = findPublisherById(id);

        Optional<Publisher> publisherByNameOptional = publisherRepository.findByNameIgnoreCase(request.name().trim());

        boolean nameConflict = publisherByNameOptional.isPresent() &&
                !publisherByNameOptional.get().getId().equals(publisherById.getId());

        if (nameConflict) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Publisher with name '%s' already exists".formatted(request.name().trim()));
        }

        Publisher publisher = mapPublisher(request, findPublisherById(id));

        return toResponse(publisherRepository.save(publisher));
    }

    @PreAuthorize("hasAuthority('publishers:delete')")
    @Transactional
    public void deletePublisherById(String id) {
        Publisher publisher = findPublisherById(id);
        if (!publisher.getBooks().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes borrar una editorial que tiene libros asociados. " +
                    "Intenta combinarla con otra y será eliminada en el proceso.");
        }
        publisherRepository.delete(publisher);
    }

    @PreAuthorize("hasAuthority('publishers:read')")
    public ExportResponse export(CustomUserDetails userDetails, ExportRequest request) {
        return exportPublishers.handle(userDetails, request);
    }

    @PreAuthorize("hasAuthority('publishers:update')")
    @Transactional
    public MergePublishersResponse merge(MergePublishersRequest request) {
        if (request.mergedPublishersIds().contains(request.targetPublisherId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La editorial resultante no debe estar incluida en las editoriales que serán eliminadas");
        }

        Publisher targetPublisher = findPublisherById(request.targetPublisherId());

        List<Publisher> mergedPublishers = findPublishersByIds(request.mergedPublishersIds());

        int movedBooks = mergedPublishers.stream()
                .flatMap(cat -> cat.getBooks().stream())
                .peek(book -> book.setPublisher(targetPublisher))
                .toList()
                .size();

        mergedPublishers.forEach(cat -> {
            targetPublisher.getBooks().addAll(cat.getBooks());
            cat.getBooks().clear();
        });

        publisherRepository.save(targetPublisher);

        publisherRepository.deleteAll(mergedPublishers);

        return MergePublishersResponse.builder()
                .targetPublisher(toResponse(targetPublisher))
                .deletedPublishers(mergedPublishers.size())
                .movedBooks(movedBooks)
                .build();
    }

    private PublisherResponse toResponse(Publisher publisher) {
        return PublisherResponse.builder()
                .id(publisher.getId().toString())
                .name(publisher.getName())
                .bookCount(publisher.getBooks().size())
                .build();
    }

    private Publisher findPublisherById(String id) {
        Optional<Publisher> publishserOptional = publisherRepository.findById(Integer.parseInt(id));
        if (publishserOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find publisher with id: %s".formatted(id));
        }
        return publishserOptional.get();
    }

    private Publisher mapPublisher(PublisherRequest request, Publisher publisher) {
        publisher.setName(request.name().trim());
        return publisher;
    }

    private List<Publisher> findPublishersByIds(Set<String> ids) {
        Set<Integer> intIds = ids.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        List<Publisher> publishers = publisherRepository.findAllById(intIds);

        if (publishers.size() != intIds.size()) {
            Set<Integer> foundIds = publishers.stream()
                    .map(Publisher::getId)
                    .collect(Collectors.toSet());

            intIds.removeAll(foundIds);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontraron las editoriales con IDs: " + intIds
            );
        }

        return publishers;
    }

}