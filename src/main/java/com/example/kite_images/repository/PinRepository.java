package com.example.kite_images.repository;

import com.example.kite_images.model.Pin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PinRepository extends JpaRepository<Pin, Long> {

    List<Pin> findAllByOrderByIdDesc();

    List<Pin> findByUserIdOrderByIdDesc(Long userId);

    List<Pin> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrderByIdDesc(String title, String category);

}