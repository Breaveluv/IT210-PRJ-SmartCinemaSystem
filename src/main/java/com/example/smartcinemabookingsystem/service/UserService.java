package com.example.smartcinemabookingsystem.service;

import com.example.smartcinemabookingsystem.model.User;
import com.example.smartcinemabookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User registerNewUser(User user) {
        // Since security is not implemented yet, we store password as is (not recommended for production)
        user.setRole(User.Role.CUSTOMER);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
