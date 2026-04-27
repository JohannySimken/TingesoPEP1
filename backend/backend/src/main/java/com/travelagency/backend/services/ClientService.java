package com.travelagency.backend.services;

import com.travelagency.backend.entities.ClientEntity;
import com.travelagency.backend.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;
    private final ClientRepository clientRepository;

    public ClientEntity register(ClientEntity client){
        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new RuntimeException("Email ya registrado " + client.getEmail());
        }
        client.setRole(ClientEntity.Role.CLIENT);
        client.setStatus(ClientEntity.Status.ACTIVE);
        client.setFailedLoginAttempts(0);
        return clientRepository.save(client);
    }

    public ClientEntity login(String email, String password){
        ClientEntity client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if(client.getStatus() == ClientEntity.Status.INACTIVE){
            throw new RuntimeException("Usuario bloqueado o inactivo");
        }
        if(!client.getPassword().equals(password)){
            client.setFailedLoginAttempts(client.getFailedLoginAttempts() + 1);
            if(client.getFailedLoginAttempts() > MAX_FAILED_LOGIN_ATTEMPTS){
                client.setStatus(ClientEntity.Status.INACTIVE);
                clientRepository.save(client);
                throw new RuntimeException("Demasiados intentos fallidos");
            }
            clientRepository.save(client);
            throw new RuntimeException("Correo o contraseña incorrectos");

        }
        client.setFailedLoginAttempts(0);
        return clientRepository.save(client);
    }


    public List<ClientEntity> findAll(){
        return clientRepository.findAll();
    }
    public ClientEntity findById(Long id){
        return clientRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    public ClientEntity findByEmail(String email){
        return clientRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    public ClientEntity update(Long id, ClientEntity clientUpdated){
        ClientEntity clientEntity = findById(id);
        if(clientUpdated.getName() != null) clientEntity.setName(clientUpdated.getName());
        if(clientUpdated.getPhone() != null) clientEntity.setPhone(clientUpdated.getPhone());
        if(clientUpdated.getNationality() != null) clientEntity.setNationality(clientUpdated.getNationality());
        if(clientUpdated.getIdentityDocument() != null) clientEntity.setIdentityDocument(clientUpdated.getIdentityDocument());
        return clientRepository.save(clientEntity);
    }

    public void deactivateClient(Long id){
        ClientEntity clientEntity = findById(id);
        clientEntity.setStatus(ClientEntity.Status.INACTIVE);
        clientRepository.save(clientEntity);
    }
}
