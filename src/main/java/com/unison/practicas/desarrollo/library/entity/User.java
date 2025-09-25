package com.unison.practicas.desarrollo.library.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "app_user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private String phoneNumber;
    private String profilePictureUrl;
    private Instant registrationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gender_id", nullable = false)
    private Gender gender;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public Set<Permission> getPermissions() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private UserAddress address;

    public String getFullName() {
        return firstName + " " + lastName;
    }

}