package com.ShortStuff.controller;

import com.ShortStuff.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid AuthRequest request) {
        String token = authService.register(request.email(), request.password());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid AuthRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/google/callback")
    public ResponseEntity<String> googleCallback(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String token = authService.loginOrRegisterGoogle(email);
        return ResponseEntity.ok(token);
    }

    public record AuthRequest(
            @Email(message = "must be a valid email")
            @NotBlank(message = "email is required")
            String email,

            @NotBlank(message = "password is required")
            String password
    ) {}
}
