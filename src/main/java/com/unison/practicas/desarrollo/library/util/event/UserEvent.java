package com.unison.practicas.desarrollo.library.util.event;

public sealed class UserEvent extends AuditEvent permits
        UserRegistered
{ }
