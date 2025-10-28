package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.audit.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Integer> {
}
