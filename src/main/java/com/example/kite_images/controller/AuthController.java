package com.example.kite_images.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.kite_images.service.PinService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.kite_images.model.User;
import com.example.kite_images.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;
    private final PinService pinService;

    public AuthController(
        UserService userService,
        PinService pinService) {

    this.userService = userService;
    this.pinService = pinService;
}

    // LOGIN PAGE
    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    // REGISTER PAGE
    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }

    // REGISTER USER
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam("profilePicture") MultipartFile file) {

        try {

            User user = new User();

            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);

            if (!file.isEmpty()) {
                user.setProfilePicture(file.getBytes());
            }

            userService.register(user);

            return "redirect:/";

        } catch (Exception e) {

            e.printStackTrace();

            return "register";
        }
    }

    // LOGIN
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        User user = userService.login(email, password);

        if (user != null) {

            session.setAttribute("userId", user.getId());

            return "redirect:/home";
        }

        model.addAttribute("error", "Invalid email or password");

        return "login";
    }

    @GetMapping("/home")
public String home(HttpSession session, Model model) {

    Long userId = (Long) session.getAttribute("userId");

    if (userId == null) {
        return "redirect:/";
    }

    User user = userService.getUser(userId);

    model.addAttribute("user", user);

    model.addAttribute("pins", pinService.getAllPins());

    return "index";
}

    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}