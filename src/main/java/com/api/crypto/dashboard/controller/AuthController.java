package com.api.crypto.dashboard.controller;

import com.api.crypto.dashboard.dto.AuthDTO;
import com.api.crypto.dashboard.dto.LoginResponseDTO;
import com.api.crypto.dashboard.dto.RegisterDTO;
import com.api.crypto.dashboard.model.User;
import com.api.crypto.dashboard.repository.UserRepository;
import com.api.crypto.dashboard.security.SecurityUser;
import com.api.crypto.dashboard.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository repository;
    @Autowired
    private TokenService tokenService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated AuthDTO login) {
        var authToken = new UsernamePasswordAuthenticationToken(login.username(), login.password());

        try {
            var auth = authenticationManager.authenticate(authToken);

            SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
            var token = tokenService.generateToken(securityUser.getUsername());

            return ResponseEntity.ok(new LoginResponseDTO(token));

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário ou senha inválidos!");
        }
    }



    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Validated RegisterDTO login) {
        if (this.repository.findByUserName(login.username()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "O Usuário informado já está cadastrado!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        String encryptedPassword = new BCryptPasswordEncoder().encode(login.password());
        User novoUsuario = new User(login.username(), encryptedPassword);

        this.repository.save(novoUsuario);

        Map<String, Object> response = new HashMap<>();
        response.put("id", novoUsuario.getId());
        response.put("userName", novoUsuario.getUserName());

        return ResponseEntity.ok(response);
    }

}
