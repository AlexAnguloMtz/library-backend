package com.unison.practicas.desarrollo.library.util.event;

public sealed class AuditEvent permits
        AuthorEvent,
        BookEvent,
        BookCategoryEvent,
        PublisherEvent
{ }