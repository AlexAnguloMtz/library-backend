package com.unison.practicas.desarrollo.library.util.factory;

import com.github.javafaker.Faker;
import com.unison.practicas.desarrollo.library.entity.State;
import com.unison.practicas.desarrollo.library.entity.UserAddress;
import com.unison.practicas.desarrollo.library.repository.StateRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
@Profile({"dev", "test"})
public class UserAddressFactory {

    private final Faker faker;
    private final StateRepository stateRepository;

    public UserAddressFactory(Faker faker, StateRepository stateRepository) {
        this.faker = faker;
        this.stateRepository = stateRepository;
    }

    public List<UserAddress> createUserAddresses(int count) {
        if (count < 0) {
            throw new RuntimeException("Count must be greater than 0, got %d".formatted(count));
        }
        
        List<State> states = stateRepository.findAll();

        return IntStream.range(1, count + 1)
                .mapToObj(i -> createUserAddress(i, CollectionHelpers.randomItem(states)))
                .toList();
    }

    private UserAddress createUserAddress(int i, State state) {
        var userAddress = new UserAddress();
        userAddress.setAddress(faker.address().streetAddress());
        userAddress.setCity(faker.address().cityName());
        userAddress.setZipCode(faker.address().zipCode());
        userAddress.setState(state);
        userAddress.setDistrict(faker.address().streetAddress());
        return userAddress;
    }

}