package com.example.kite_images.controller;

import com.example.kite_images.model.User;
import com.example.kite_images.model.Notification;
import com.example.kite_images.service.UserService;
import com.example.kite_images.service.NotificationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class NotificationController {

    private final UserService userService;
    private final NotificationService notificationService;

    public NotificationController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);
        List<Notification> notifications = notificationService.getNotifications(userId);

        notificationService.markAllAsRead(userId);

        model.addAttribute("user", user);
        model.addAttribute("notifications", notifications);

        return "notifications";
    }

    @GetMapping("/api/notifications/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unreadCount(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("count", 0);
            return ResponseEntity.ok(resp);
        }

        int count = notificationService.getUnreadCount(userId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("count", count);
        return ResponseEntity.ok(resp);
    }
}
