package com.unison.practicas.desarrollo.library.entity.common;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "country_seq")
    @SequenceGenerator(name = "country_seq", sequenceName = "country_seq", allocationSize = 1)
    private Integer id;
    private String iso;
    private String name;
    private String nicename;
    private String iso3;
    private Short numcode;
    private Integer phonecode;

}