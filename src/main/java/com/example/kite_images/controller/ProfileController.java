package com.example.kite_images.controller;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.kite_images.model.User;
import com.example.kite_images.service.UserService;
import com.example.kite_images.service.PinService;

import org.springframework.ui.Model;

@Controller
public class ProfileController {

    private final UserService userService;
    private final PinService pinService;


    public ProfileController(UserService userService, PinService pinService) {
        this.userService = userService;
        this.pinService = pinService;
    }


    // PROFILE PAGE
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);

        int pinCount = pinService.getPinsByUserId(userId).size();

        model.addAttribute("user", user);
        model.addAttribute("pinCount", pinCount);

        return "profile";
    }


    // UPDATE PROFILE PICTURE
    @PostMapping("/profile/upload")
    public String uploadProfilePicture(
            @RequestParam("profilePicture") MultipartFile file,
            HttpSession session) {

        try {

            Long userId = (Long) session.getAttribute("userId");

            if (userId == null) {
                return "redirect:/";
            }

            User user = userService.getUser(userId);

            if (!file.isEmpty()) {

                user.setProfilePicture(file.getBytes());

                userService.updateUser(user);
            }

            return "redirect:/profile";

        } catch (Exception e) {

            e.printStackTrace();

            return "redirect:/profile";
        }
    }


    // DISPLAY PROFILE IMAGE
    @GetMapping("/profile/image/{id}")
    @ResponseBody
    public byte[] profileImage(@PathVariable Long id) {

        User user = userService.getUser(id);

        if (user != null && user.getProfilePicture() != null) {
            return user.getProfilePicture();
        }

        return new byte[0];
    }


    // EDIT PROFILE PAGE
    @GetMapping("/profile/edit")
    public String editProfile(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);

        model.addAttribute("user", user);

        return "edit-profile";
    }


    // SAVE PROFILE EDIT
    @PostMapping("/profile/edit")
    public String saveProfile(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(value = "password", required = false) String password,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);

        user.setName(name);
        user.setEmail(email);

        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(password.trim());
        }

        userService.updateUser(user);

        return "redirect:/profile";
    }
}