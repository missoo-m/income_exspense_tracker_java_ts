package com.example.expensetracker.repository;

import com.example.expensetracker.model.Notification;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Optional<Notification> findByIdAndUser(Long id, User user);
    boolean existsByUserAndTypeAndMonthAndGeneralCategory(User user, String type, String month, String generalCategory);
    void deleteByUser(User user);
}

