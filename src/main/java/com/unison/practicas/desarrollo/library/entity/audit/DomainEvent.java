package com.unison.practicas.desarrollo.library.entity.audit;

import com.unison.practicas.desarrollo.library.util.StringHelper;
import lombok.Data;

@Data
public class DomainEvent {

    private final String resourceId;

    public String getEventTypeId() {
        return StringHelper.pascalCaseToSnakeCase(
                getClass().getSimpleName()
        ).toUpperCase();
    }

}