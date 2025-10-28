package com.unison.practicas.desarrollo.library.util.events;

public sealed class BookCategoryEvent extends AuditEvent
        permits BookCategoryCreated {}