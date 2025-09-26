package com.unison.practicas.desarrollo.library.service.user;

import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import com.unison.practicas.desarrollo.library.dto.user.response.UserOptionsResponse;
import com.unison.practicas.desarrollo.library.entity.Gender;
import com.unison.practicas.desarrollo.library.entity.Role;
import com.unison.practicas.desarrollo.library.entity.State;
import com.unison.practicas.desarrollo.library.repository.GenderRepository;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.repository.StateRepository;
import org.springframework.stereotype.Component;

@Component
public class GetUserOptions {

    private final RoleRepository roleRepository;
    private final StateRepository stateRepository;
    private final GenderRepository genderRepository;

    public GetUserOptions(RoleRepository roleRepository, StateRepository stateRepository, GenderRepository genderRepository) {
        this.roleRepository = roleRepository;
        this.stateRepository = stateRepository;
        this.genderRepository = genderRepository;
    }

    public UserOptionsResponse get() {
        Iterable<OptionResponse> roles = roleRepository.findAll().stream()
                .map(this::toOption)
                .sorted((a, b) -> a.label().compareToIgnoreCase(b.label()))
                .toList();

        Iterable<OptionResponse> states = stateRepository.findAll().stream()
                .map(this::toOption)
                .sorted((a, b) -> a.label().compareToIgnoreCase(b.label()))
                .toList();

        Iterable<OptionResponse> genders = genderRepository.findAll().stream()
                .map(this::toOption)
                .sorted((a, b) -> a.label().compareToIgnoreCase(b.label()))
                .toList();

        return UserOptionsResponse.builder()
                .roles(roles)
                .states(states)
                .genders(genders)
                .build();
    }

    private OptionResponse toOption(Role role) {
        return OptionResponse.builder()
                .value(role.getId().toString())
                .label(role.getName())
                .build();
    }

    private OptionResponse toOption(State state) {
        return OptionResponse.builder()
                .value(state.getId().toString())
                .label(state.getName())
                .build();
    }

    private OptionResponse toOption(Gender gender) {
        return OptionResponse.builder()
                .value(gender.getId().toString())
                .label(gender.getName())
                .build();
    }

}