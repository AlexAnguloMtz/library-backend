package com.unison.practicas.desarrollo.library.util.event;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class PublishersMerged extends PublisherEvent {

    @Builder
    public record MergedPublisher(
            String publisherId,
            String name,
            Integer booksBeforeMerge,
            Integer booksAfterMerge
    ) {}

    private final MergedPublisher targetPublisher;
    private final List<MergedPublisher> mergedPublishers;

}