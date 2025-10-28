package com.unison.practicas.desarrollo.library.entity.book;

import com.unison.practicas.desarrollo.library.entity.audit.DomainEvent;

public class BookCategoryCreated extends DomainEvent {
    public BookCategoryCreated(String resourceId) {
        super(resourceId);
    }
}