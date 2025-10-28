package com.unison.practicas.desarrollo.library.util.events;

import com.unison.practicas.desarrollo.library.util.StringHelper;

public sealed class AuditEvent permits BookCategoryEvent {

    public String getEventTypeId() {
        String snakeCaseClassName = StringHelper.pascalCaseToSnakeCase(getClass().getSimpleName());
        return snakeCaseClassName.toUpperCase();
    }

}