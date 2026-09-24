package com.example.kite_images.repository;

import com.example.kite_images.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserIdAndPinId(Long userId, Long pinId);

    void deleteByUserIdAndPinId(Long userId, Long pinId);

    int countByPinId(Long pinId);
}
