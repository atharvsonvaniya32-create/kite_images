package com.example.kite_images.service;

import com.example.kite_images.model.Save;
import com.example.kite_images.model.User;
import com.example.kite_images.model.Pin;
import com.example.kite_images.repository.SaveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaveService {

    private final SaveRepository saveRepository;

    public SaveService(SaveRepository saveRepository) {
        this.saveRepository = saveRepository;
    }

    public boolean hasSaved(Long userId, Long pinId) {
        return saveRepository.existsByUserIdAndPinId(userId, pinId);
    }

    @Transactional
    public boolean toggleSave(User user, Pin pin) {
        if (saveRepository.existsByUserIdAndPinId(user.getId(), pin.getId())) {
            saveRepository.deleteByUserIdAndPinId(user.getId(), pin.getId());
            pin.setSaveCount(Math.max(0, pin.getSaveCount() - 1));
            return false;
        } else {
            saveRepository.save(new Save(user, pin));
            pin.setSaveCount(pin.getSaveCount() + 1);
            return true;
        }
    }

    public int getSaveCount(Long pinId) {
        return saveRepository.countByPinId(pinId);
    }

    public List<Pin> getSavedPins(Long userId) {
        return saveRepository.findByUserIdOrderByIdDesc(userId)
                .stream()
                .map(Save::getPin)
                .collect(Collectors.toList());
    }
}
