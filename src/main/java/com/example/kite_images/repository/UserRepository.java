package com.example.kite_images.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kite_images.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByNameContainingIgnoreCaseOrderByIdAsc(String name);

}