package com.travelagency.backend.controllers;

import com.travelagency.backend.entities.UserEntity;
import com.travelagency.backend.repositories.UserRepository;
import com.travelagency.backend.services.UserService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping(value = "/register")
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            userService.register(user)
        );
    }

    @PostMapping(value = "/login")
    public ResponseEntity<UserEntity> login(@RequestBody UserEntity user) {
        return ResponseEntity.ok(
            userService.login(user.getEmail(), user.getPassword())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserEntity>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> update(
        @PathVariable Long id,
        @RequestBody UserEntity userUpdated
    ) {
        return ResponseEntity.ok(userService.update(id, userUpdated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(
        @RequestBody Map<String, Object> userData
    ) {
        String keycloakId = (String) userData.get("keycloakId");
        String email = (String) userData.get("email");
        String name = (String) userData.get("name");

        // Leer roles desde el token
        List<String> roles = (List<String>) userData.getOrDefault(
            "roles",
            List.of()
        );
        UserEntity.Role role =
            roles.contains("ROLE_ADMIN") || roles.contains("ADMIN")
                ? UserEntity.Role.ADMIN
                : UserEntity.Role.CLIENT;

        UserEntity user = userRepository
            .findByKeycloakId(keycloakId)
            .orElseGet(() ->
                userRepository
                    .findByEmail(email)
                    .orElse(
                        UserEntity.builder()
                            .email(email)
                            .name(name)
                            .password("KEYCLOAK")
                            .role(role)
                            .status(UserEntity.Status.ACTIVE)
                            .build()
                    )
            );

        user.setKeycloakId(keycloakId);
        user.setName(name);
        // Actualizar rol si ya existía el usuario
        user.setRole(role);
        UserEntity saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }
}
