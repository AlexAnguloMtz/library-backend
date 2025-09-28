package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.common.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Integer> {
}