package com.example.kite_images.controller;

import com.example.kite_images.model.User;
import com.example.kite_images.model.Message;
import com.example.kite_images.service.UserService;
import com.example.kite_images.service.FollowService;
import com.example.kite_images.service.MessageService;
import com.example.kite_images.service.PinService;
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
public class ConnectController {

    private final UserService userService;
    private final FollowService followService;
    private final MessageService messageService;
    private final PinService pinService;
    private final NotificationService notificationService;

    public ConnectController(
            UserService userService,
            FollowService followService,
            MessageService messageService,
            PinService pinService,
            NotificationService notificationService) {
        this.userService = userService;
        this.followService = followService;
        this.messageService = messageService;
        this.pinService = pinService;
        this.notificationService = notificationService;
    }


    // CONNECT PAGE - SEARCH PEOPLE
    @GetMapping("/connect")
    public String connect(
            @RequestParam(value = "q", required = false) String query,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);

        List<User> users;
        if (query != null && !query.trim().isEmpty()) {
            users = userService.searchUsers(query.trim());
            model.addAttribute("query", query.trim());
        } else {
            users = List.of();
        }

        model.addAttribute("user", user);
        model.addAttribute("users", users);

        return "connect";
    }


    // USER PROFILE PAGE (OTHER USERS)
    @GetMapping("/user/{id}")
    public String userProfile(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);
        User profileUser = userService.getUser(id);

        if (profileUser == null) {
            return "redirect:/connect";
        }

        boolean isFollowing = followService.isFollowing(userId, id);
        int followerCount = followService.getFollowerCount(id);
        int followingCount = followService.getFollowingCount(id);

        model.addAttribute("user", user);
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("followerCount", followerCount);
        model.addAttribute("followingCount", followingCount);
        model.addAttribute("pins", pinService.getPinsByUserId(id));

        return "user-profile";
    }


    // TOGGLE FOLLOW
    @PostMapping("/user/{id}/follow")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFollow(
            @PathVariable Long id,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        User follower = userService.getUser(userId);
        User following = userService.getUser(id);

        if (following == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isFollowing = followService.toggleFollow(follower, following);

        if (isFollowing) {
            notificationService.create(
                following, follower, "follow",
                "started following you", null);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("following", isFollowing);
        response.put("followerCount", followService.getFollowerCount(id));

        return ResponseEntity.ok(response);
    }


    // MESSAGES PAGE - CONVERSATIONS LIST
    @GetMapping("/messages")
    public String messages(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);
        List<Map<String, Object>> conversations = messageService.getConversations(userId);
        notificationService.markAllMessageNotificationsAsRead(userId);

        model.addAttribute("user", user);
        model.addAttribute("conversations", conversations);

        return "messages";
    }


    // CHAT WITH A USER
    @GetMapping("/messages/{id}")
    public String chat(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);
        User otherUser = userService.getUser(id);

        if (otherUser == null) {
            return "redirect:/messages";
        }

        List<Message> conversation = messageService.getConversation(userId, id);
        messageService.markAsRead(id, userId);
        notificationService.markMessageNotificationsAsRead(userId, id);

        model.addAttribute("user", user);
        model.addAttribute("otherUser", otherUser);
        model.addAttribute("messages", conversation);

        return "chat";
    }


    // SEND MESSAGE (AJAX)
    @PostMapping("/messages/{id}/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable Long id,
            @RequestParam String text,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        User sender = userService.getUser(userId);
        User receiver = userService.getUser(id);

        if (receiver == null || text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Message message = messageService.sendMessage(sender, receiver, text.trim());

        notificationService.create(
            receiver, sender, "message",
            "sent you a message", null);

        Map<String, Object> response = new HashMap<>();
        response.put("messageId", message.getId());
        response.put("text", message.getText());
        response.put("senderName", sender.getName());
        response.put("createdAt", message.getCreatedAt().toString());

        return ResponseEntity.ok(response);
    }


    // DELETE MESSAGE (AJAX)
    @PostMapping("/messages/delete/{msgId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable Long msgId,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        Message message = messageService.getMessage(msgId);

        if (message == null) {
            return ResponseEntity.notFound().build();
        }

        if (!message.getSender().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        messageService.deleteMessage(msgId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        return ResponseEntity.ok(response);
    }
}
