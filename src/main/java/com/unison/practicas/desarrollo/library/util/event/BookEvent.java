package com.unison.practicas.desarrollo.library.util.event;

public sealed class BookEvent extends AuditEvent permits
        BookCreated,
        BookDeleted
{ }