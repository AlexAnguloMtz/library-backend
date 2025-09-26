package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.dto.auth.LoginForm;
import com.unison.practicas.desarrollo.library.dto.auth.LoginResponse;
import com.unison.practicas.desarrollo.library.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginForm form) {
        return authService.login(form);
    }

}