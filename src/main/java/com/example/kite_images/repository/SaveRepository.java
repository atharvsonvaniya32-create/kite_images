package com.example.kite_images.repository;

import com.example.kite_images.model.Save;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaveRepository extends JpaRepository<Save, Long> {

    boolean existsByUserIdAndPinId(Long userId, Long pinId);

    void deleteByUserIdAndPinId(Long userId, Long pinId);

    int countByPinId(Long pinId);

    List<Save> findByUserIdOrderByIdDesc(Long userId);
}
