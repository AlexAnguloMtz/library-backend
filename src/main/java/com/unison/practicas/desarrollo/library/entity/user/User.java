package com.unison.practicas.desarrollo.library.entity.user;

import com.unison.practicas.desarrollo.library.entity.common.Gender;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

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
    private LocalDate dateOfBirth;
    private Instant registrationDate;
    private Boolean canLogin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gender_id", nullable = false)
    private Gender gender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    public Set<Permission> getPermissions() {
        return role.getPermissions();
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

    public void setAddress(UserAddress address) {
        this.address = address;
        address.setUser(this);
    }

    public Optional<UserAddress> getAddress() {
        return Optional.ofNullable(address);
    }

    public Optional<String> getProfilePictureUrl() {
        return Optional.ofNullable(profilePictureUrl);
    }

}