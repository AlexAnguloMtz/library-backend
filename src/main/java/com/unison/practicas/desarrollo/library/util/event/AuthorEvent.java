package com.unison.practicas.desarrollo.library.util.event;

public sealed class AuthorEvent extends AuditEvent permits
        AuthorCreated,
        AuthorUpdated,
        AuthorDeleted
{ }