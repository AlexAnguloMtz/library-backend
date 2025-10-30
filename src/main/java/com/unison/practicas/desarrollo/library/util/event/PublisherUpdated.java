package com.unison.practicas.desarrollo.library.util.event;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class PublisherUpdated extends PublisherEvent {

    @Builder
    public record Fields (String name) {}

    private final String publisherId;
    private final Fields oldValues;
    private final Fields newValues;

}