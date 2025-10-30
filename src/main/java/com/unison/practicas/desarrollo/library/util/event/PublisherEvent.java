package com.unison.practicas.desarrollo.library.util.event;

import lombok.*;

public sealed class PublisherEvent extends AuditEvent permits
        PublisherCreated,
        PublisherUpdated,
        PublisherDeleted,
        PublishersMerged
{ }