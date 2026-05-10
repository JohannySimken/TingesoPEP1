package com.travelagency.backend.services;

import com.travelagency.backend.entities.UserEntity;
import com.travelagency.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;
    private final UserRepository userRepository;

    public UserEntity register(UserEntity user){
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email ya registrado " + user.getEmail());
        }
        user.setRole(UserEntity.Role.CLIENT);
        user.setStatus(UserEntity.Status.ACTIVE);
        user.setFailedLoginAttempts(0);
        return userRepository.save(user);
    }

    public UserEntity login(String email, String password){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if(user.getStatus() == UserEntity.Status.INACTIVE){
            throw new RuntimeException("Usuario bloqueado o inactivo");
        }
        if(!user.getPassword().equals(password)){
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if(user.getFailedLoginAttempts() > MAX_FAILED_LOGIN_ATTEMPTS){
                user.setStatus(UserEntity.Status.INACTIVE);
                userRepository.save(user);
                throw new RuntimeException("Demasiados intentos fallidos");
            }
            userRepository.save(user);
            throw new RuntimeException("Correo o contraseña incorrectos");

        }
        user.setFailedLoginAttempts(0);
        return userRepository.save(user);
    }


    public List<UserEntity> findAll(){
        return userRepository.findAll();
    }

    public UserEntity findById(Long id){
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public UserEntity update(Long id, UserEntity userUpdated){
        UserEntity userEntity = findById(id);
        if(userUpdated.getName() != null) userEntity.setName(userUpdated.getName());
        if(userUpdated.getPhone() != null) userEntity.setPhone(userUpdated.getPhone());
        if(userUpdated.getNationality() != null) userEntity.setNationality(userUpdated.getNationality());
        if(userUpdated.getIdentityDocument() != null) userEntity.setIdentityDocument(userUpdated.getIdentityDocument());
        return userRepository.save(userEntity);
    }

    public void deactivateUser(Long id){
        UserEntity userEntity = findById(id);
        userEntity.setStatus(UserEntity.Status.INACTIVE);
        userRepository.save(userEntity);
    }
}
