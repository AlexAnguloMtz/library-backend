package com.unison.practicas.desarrollo.library.util.event;

public sealed class BookCategoryEvent extends AuditEvent
        permits BookCategoryCreated, BookCategoryUpdated, BookCategoryDeleted {}