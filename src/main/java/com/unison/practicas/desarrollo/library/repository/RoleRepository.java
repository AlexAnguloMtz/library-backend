package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
