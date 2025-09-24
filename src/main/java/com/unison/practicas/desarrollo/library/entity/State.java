package com.unison.practicas.desarrollo.library.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String code;

}