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
    public record MergedPublisher(String categoryId, String name) {}

    private final MergedPublisher targetPublisher;
    private final List<MergedPublisher> mergedPublishers;
    private final Integer booksMoved;

}