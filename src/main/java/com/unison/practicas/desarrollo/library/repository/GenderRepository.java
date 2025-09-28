package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.common.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenderRepository extends JpaRepository<Gender, Integer> {
}
