package com.unison.practicas.desarrollo.library.entity.user;

import com.unison.practicas.desarrollo.library.entity.common.State;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    private String city;

    private String zipCode;

    private String district;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

}