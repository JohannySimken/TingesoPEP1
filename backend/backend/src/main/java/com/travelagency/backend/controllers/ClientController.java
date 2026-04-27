package com.travelagency.backend.controllers;

import com.travelagency.backend.entities.ClientEntity;
import com.travelagency.backend.services.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping(value = "/register")
    public ResponseEntity<ClientEntity> register(@RequestBody ClientEntity client){
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.register(client));
    }

    @PostMapping(value = "/login")
    public ResponseEntity<ClientEntity> login(@RequestBody ClientEntity client){
        return ResponseEntity.ok(clientService.login(client.getEmail(), client.getPassword()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientEntity> findById(@PathVariable Long id){
        return ResponseEntity.ok(clientService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ClientEntity>> findAll(){
        return ResponseEntity.ok(clientService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientEntity> update(@PathVariable Long id, @RequestBody ClientEntity clientUpdated){
        return ResponseEntity.ok(clientService.update(id, clientUpdated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id){
        clientService.deactivateClient(id);
        return ResponseEntity.noContent().build();
    }

}
