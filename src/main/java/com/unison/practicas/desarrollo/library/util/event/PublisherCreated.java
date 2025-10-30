package com.unison.practicas.desarrollo.library.util.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public final class PublisherCreated extends PublisherEvent {
    private final String publisherId;
    private final String name;
}