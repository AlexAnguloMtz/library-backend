package com.unison.practicas.desarrollo.library.entity.audit;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class AuditResourceType {

    @Id
    private String id;

    @OneToMany(mappedBy = "resourceType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditEventType> eventTypes;

}