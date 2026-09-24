package com.example.kite_images.controller;

import com.example.kite_images.model.Pin;
import com.example.kite_images.model.User;
import com.example.kite_images.model.Comment;
import com.example.kite_images.service.PinService;
import com.example.kite_images.service.UserService;
import com.example.kite_images.service.CommentService;
import com.example.kite_images.service.LikeService;
import com.example.kite_images.service.SaveService;
import com.example.kite_images.service.NotificationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PinController {

    private final PinService pinService;
    private final UserService userService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final SaveService saveService;
    private final NotificationService notificationService;

    public PinController(
            PinService pinService,
            UserService userService,
            CommentService commentService,
            LikeService likeService,
            SaveService saveService,
            NotificationService notificationService) {
        this.pinService = pinService;
        this.userService = userService;
        this.commentService = commentService;
        this.likeService = likeService;
        this.saveService = saveService;
        this.notificationService = notificationService;
    }

    // Open Create Pin page
    @GetMapping("/create")
    public String createPage(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        model.addAttribute("pin", new Pin());

        return "create";
    }


    // Upload Pin
    @PostMapping("/pins/upload")
    public String uploadPin(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("image") MultipartFile file,
            HttpSession session) {

        try {

            Long userId = (Long) session.getAttribute("userId");

            if (userId == null) {
                return "redirect:/";
            }

            if (file.isEmpty()) {
                return "redirect:/create";
            }

            User user = userService.getUser(userId);

            if (user == null) {
                return "redirect:/";
            }

            Pin pin = new Pin();

            pin.setTitle(title);
            pin.setCategory(category);
            pin.setImage(file.getBytes());
            pin.setUser(user);

            pinService.savePin(pin);

            return "redirect:/home";

        } catch (Exception e) {

            e.printStackTrace();

            return "redirect:/create";
        }
    }


    // Display uploaded image
    @GetMapping("/pins/image/{id}")
    @ResponseBody
    public byte[] pinImage(@PathVariable Long id) {

        Pin pin = pinService.getPin(id);

        if (pin != null && pin.getImage() != null) {
            return pin.getImage();
        }

        return new byte[0];
    }


    // Delete Pin
    @GetMapping("/pins/delete/{id}")
    public String deletePin(
            @PathVariable Long id,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        Pin pin = pinService.getPin(id);

        if (pin != null &&
                pin.getUser() != null &&
                pin.getUser().getId().equals(userId)) {

            pinService.deletePin(id);
        }

        return "redirect:/my-uploads";
    }


    // Edit Pin Page
    @GetMapping("/pin/edit/{id}")
    public String editPinPage(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        Pin pin = pinService.getPin(id);

        if (pin == null ||
                pin.getUser() == null ||
                !pin.getUser().getId().equals(userId)) {
            return "redirect:/my-uploads";
        }

        model.addAttribute("pin", pin);

        return "edit-pin";
    }


    // Save Edit
    @PostMapping("/pin/edit/{id}")
    public String saveEdit(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam(value = "image", required = false) MultipartFile file,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        Pin pin = pinService.getPin(id);

        if (pin == null ||
                pin.getUser() == null ||
                !pin.getUser().getId().equals(userId)) {
            return "redirect:/my-uploads";
        }

        try {
            pin.setTitle(title);
            pin.setCategory(category);

            if (file != null && !file.isEmpty()) {
                pin.setImage(file.getBytes());
            }

            pinService.savePin(pin);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/my-uploads";
    }


    // =====================
    // PIN DETAIL PAGE
    // =====================

    @GetMapping("/pin/{id}")
    public String pinDetail(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);
        Pin pin = pinService.getPin(id);

        if (pin == null) {
            return "redirect:/home";
        }

        List<Comment> comments = commentService.getCommentsByPinId(id);
        boolean liked = likeService.hasLiked(userId, id);
        boolean saved = saveService.hasSaved(userId, id);
        List<Pin> relatedPins = pinService.getAllPins();
        notificationService.markPinNotificationsAsRead(userId, id);

        model.addAttribute("user", user);
        model.addAttribute("pin", pin);
        model.addAttribute("comments", comments);
        model.addAttribute("liked", liked);
        model.addAttribute("saved", saved);
        model.addAttribute("relatedPins", relatedPins);

        return "pin-detail";
    }


    // =====================
    // LIKE / UNLIKE
    // =====================

    @PostMapping("/pin/{id}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long id,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "not logged in");
            return ResponseEntity.status(401).body(err);
        }

        User user = userService.getUser(userId);
        Pin pin = pinService.getPin(id);

        if (pin == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isLiked = likeService.toggleLike(user, pin);
        pinService.savePin(pin);

        if (isLiked && pin.getUser() != null) {
            notificationService.create(
                pin.getUser(), user, "like",
                "liked your pin", pin.getId());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("liked", isLiked);
        response.put("likeCount", pin.getLikeCount());

        return ResponseEntity.ok(response);
    }


    // =====================
    // SAVE / UNSAVE
    // =====================

    @PostMapping("/pin/{id}/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSave(
            @PathVariable Long id,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "not logged in");
            return ResponseEntity.status(401).body(err);
        }

        User user = userService.getUser(userId);
        Pin pin = pinService.getPin(id);

        if (pin == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isSaved = saveService.toggleSave(user, pin);
        pinService.savePin(pin);

        Map<String, Object> response = new HashMap<>();
        response.put("saved", isSaved);
        response.put("saveCount", pin.getSaveCount());

        return ResponseEntity.ok(response);
    }


    // =====================
    // ADD COMMENT
    // =====================

    @PostMapping("/pin/{id}/comment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long id,
            @RequestParam String text,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "not logged in");
            return ResponseEntity.status(401).body(err);
        }

        User user = userService.getUser(userId);
        Pin pin = pinService.getPin(id);

        if (pin == null) {
            return ResponseEntity.notFound().build();
        }

        if (text == null || text.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "comment cannot be empty");
            return ResponseEntity.badRequest().body(err);
        }

        Comment comment = new Comment();
        comment.setText(text.trim());
        comment.setUser(user);
        comment.setPin(pin);

        commentService.saveComment(comment);

        pin.setCommentCount(commentService.getCommentCount(id));
        pinService.savePin(pin);

        if (pin.getUser() != null) {
            notificationService.create(
                pin.getUser(), user, "comment",
                "commented on your pin", pin.getId());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("commentId", comment.getId());
        response.put("commentText", comment.getText());
        response.put("userName", user.getName());
        response.put("commentCount", pin.getCommentCount());
        response.put("createdAt", comment.getCreatedAt().toString());

        return ResponseEntity.ok(response);
    }


    // =====================
    // MY UPLOADS PAGE
    // =====================

    @GetMapping("/my-uploads")
    public String myUploads(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);
        List<Pin> pins = pinService.getPinsByUserId(userId);

        model.addAttribute("user", user);
        model.addAttribute("pins", pins);

        return "my-uploads";
    }


    // =====================
    // SAVED PAGE
    // =====================

    @GetMapping("/saved")
    public String savedPins(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);
        List<Pin> pins = saveService.getSavedPins(userId);

        model.addAttribute("user", user);
        model.addAttribute("pins", pins);

        return "saved";
    }


    // =====================
    // SEARCH
    // =====================

    @GetMapping("/search")
    public String search(
            @RequestParam("query") String query,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.getUser(userId);
        List<Pin> pins = pinService.searchPins(query);

        model.addAttribute("user", user);
        model.addAttribute("pins", pins);
        model.addAttribute("query", query);

        return "search";
    }
}
