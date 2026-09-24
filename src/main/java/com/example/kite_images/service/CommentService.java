package com.example.kite_images.service;

import com.example.kite_images.model.Comment;
import com.example.kite_images.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByPinId(Long pinId) {
        return commentRepository.findByPinIdOrderByCreatedAtDesc(pinId);
    }

    public int getCommentCount(Long pinId) {
        return commentRepository.countByPinId(pinId);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
