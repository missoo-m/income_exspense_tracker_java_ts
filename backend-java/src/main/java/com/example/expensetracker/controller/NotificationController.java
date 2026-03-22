package com.example.expensetracker.controller;

import com.example.expensetracker.model.Notification;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        List<Notification> list = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@AuthenticationPrincipal User user,
                                      @PathVariable("id") Long id) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        return notificationRepository.findByIdAndUser(id, user)
                .map(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                    return ResponseEntity.ok(n);
                })
                .<ResponseEntity<?>>map(r -> r)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Уведомление не найдено")));
    }
}

