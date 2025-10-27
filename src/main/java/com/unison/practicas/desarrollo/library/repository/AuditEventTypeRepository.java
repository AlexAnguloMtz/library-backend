package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.audit.AuditEventType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventTypeRepository extends JpaRepository<AuditEventType, String> {
}