package com.example.shelter.controller;

import com.example.shelter.model.User;
import com.example.shelter.service.UserService;
import com.example.shelter.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService users;
    private final CurrentUser current;
    public AuthController(UserService users, CurrentUser current) {
        this.users = users; this.current = current;
    }
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@RequestBody User user) { return users.register(user); }
    @GetMapping("/me")
    public User me() { return current.user(); }
}
