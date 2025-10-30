package com.unison.practicas.desarrollo.library.util.event;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class UserLoggedIn extends UserEvent {
    private final String userId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String role;
}