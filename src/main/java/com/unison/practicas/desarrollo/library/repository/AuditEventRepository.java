package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.audit.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Integer> {
}
