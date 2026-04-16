package com.co.ucentral.gestionResiduos.back.user;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

        public User getUserByEmail(String email) {
            return userRepository.findByEmail(email).orElse(null); // Retorna el usuario encontrado o null si no se encuentra
        }

        public List<User> getAllUsers() {
            return userRepository.findAll(); // Retorna la lista de todos los usuarios
        }

}
