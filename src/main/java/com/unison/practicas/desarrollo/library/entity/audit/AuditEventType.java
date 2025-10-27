package com.unison.practicas.desarrollo.library.entity.audit;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class AuditEventType {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private AuditResourceType resourceType;

}