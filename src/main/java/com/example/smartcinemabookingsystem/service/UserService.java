package com.example.smartcinemabookingsystem.service;

import com.example.smartcinemabookingsystem.model.User;
import com.example.smartcinemabookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserProfile(User user) {
        // In a real application, you'd fetch the existing user, update fields, and then save.
        // This prevents overwriting fields that are not part of the form.
        // For simplicity, we're assuming the 'user' object from the form contains all updatable fields.
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // New methods for Admin user management
    public User saveUser(User user) {
        // For admin, allow setting role and potentially updating password (not hashed here yet)
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
