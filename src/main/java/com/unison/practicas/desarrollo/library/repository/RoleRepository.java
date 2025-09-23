package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
