package com.unison.practicas.desarrollo.library.util.event;

public sealed class AuditEvent permits
        UserEvent,
        AuthorEvent,
        BookEvent,
        BookCategoryEvent,
        PublisherEvent
{ }