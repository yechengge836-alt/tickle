package com.liushuai.ticket.auth;

import com.liushuai.ticket.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LoginUser> register(@Valid @RequestBody AuthRequest request) {
        return ApiResponse.ok(service.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginSession> login(@Valid @RequestBody AuthRequest request) {
        return ApiResponse.ok(service.login(request));
    }
}
