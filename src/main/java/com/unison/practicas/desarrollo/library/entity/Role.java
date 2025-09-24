package com.unison.practicas.desarrollo.library.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_role")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String slug;

}