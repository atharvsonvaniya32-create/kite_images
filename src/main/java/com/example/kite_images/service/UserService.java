package com.example.kite_images.service;


import org.springframework.stereotype.Service;

import com.example.kite_images.model.User;
import com.example.kite_images.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User register(User user) {
        return userRepository.save(user);
    }


    public User login(String email, String password) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }


    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }


    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public List<User> searchUsers(String name) {
        return userRepository.findByNameContainingIgnoreCaseOrderByIdAsc(name);
    }
}