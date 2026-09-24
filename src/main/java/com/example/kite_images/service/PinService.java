package com.example.kite_images.service;

import com.example.kite_images.model.Pin;
import com.example.kite_images.repository.PinRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PinService {

    private final PinRepository pinRepository;

    public PinService(PinRepository pinRepository) {
        this.pinRepository = pinRepository;
    }

    public Pin savePin(Pin pin) {
        return pinRepository.save(pin);
    }

    public List<Pin> getAllPins() {
        return pinRepository.findAllByOrderByIdDesc();
    }

    public Pin getPin(Long id) {
        return pinRepository.findById(id).orElse(null);
    }

    public void deletePin(Long id) {
        pinRepository.deleteById(id);
    }

    public List<Pin> getPinsByUserId(Long userId) {
        return pinRepository.findByUserIdOrderByIdDesc(userId);
    }

    public List<Pin> searchPins(String query) {
        return pinRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrderByIdDesc(query, query);
    }
}