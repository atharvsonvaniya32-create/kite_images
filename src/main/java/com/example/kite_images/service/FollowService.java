package com.example.kite_images.service;

import com.example.kite_images.model.Follow;
import com.example.kite_images.model.User;
import com.example.kite_images.repository.FollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowService {

    private final FollowRepository followRepository;

    public FollowService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Transactional
    public boolean toggleFollow(User follower, User following) {
        if (followRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId())) {
            followRepository.deleteByFollowerIdAndFollowingId(follower.getId(), following.getId());
            return false;
        } else {
            followRepository.save(new Follow(follower, following));
            return true;
        }
    }

    public int getFollowerCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    public int getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    public List<User> getFollowers(Long userId) {
        return followRepository.findByFollowingId(userId)
                .stream()
                .map(Follow::getFollower)
                .collect(Collectors.toList());
    }

    public List<User> getFollowing(Long userId) {
        return followRepository.findByFollowerId(userId)
                .stream()
                .map(Follow::getFollowing)
                .collect(Collectors.toList());
    }
}
