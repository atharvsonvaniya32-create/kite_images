package com.example.kite_images.service;

import com.example.kite_images.model.Like;
import com.example.kite_images.model.User;
import com.example.kite_images.model.Pin;
import com.example.kite_images.repository.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public boolean hasLiked(Long userId, Long pinId) {
        return likeRepository.existsByUserIdAndPinId(userId, pinId);
    }

    @Transactional
    public boolean toggleLike(User user, Pin pin) {
        if (likeRepository.existsByUserIdAndPinId(user.getId(), pin.getId())) {
            likeRepository.deleteByUserIdAndPinId(user.getId(), pin.getId());
            pin.setLikeCount(Math.max(0, pin.getLikeCount() - 1));
            return false;
        } else {
            likeRepository.save(new Like(user, pin));
            pin.setLikeCount(pin.getLikeCount() + 1);
            return true;
        }
    }

    public int getLikeCount(Long pinId) {
        return likeRepository.countByPinId(pinId);
    }
}
