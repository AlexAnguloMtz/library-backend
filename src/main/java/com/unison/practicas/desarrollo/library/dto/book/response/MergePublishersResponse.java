package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record MergePublishersResponse(
        PublisherResponse targetPublisher,
        int deletedPublishers,
        int movedBooks
) {
}