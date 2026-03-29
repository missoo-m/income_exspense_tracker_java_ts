package com.example.expensetracker.integration.controller;

import com.example.expensetracker.controller.NotificationController;
import com.example.expensetracker.model.Notification;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationController notificationController;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("Тестовый Пользователь")
                .email("test@example.com")
                .role(User.Role.NORMAL)
                .build();

        testNotification = Notification.builder()
                .id(1L)
                .user(testUser)
                .type("BUDGET_EXCEEDED")
                .message("Бюджет превышен")
                .read(false)
                .build();
    }

    @Test
    void list_ShouldReturnNotifications_WhenUserHasNotifications() {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(notifications);

        ResponseEntity<?> response = notificationController.list(testUser);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<Notification> body = (List<Notification>) response.getBody();
        assertThat(body).hasSize(1);
    }

    @Test
    void list_ShouldReturnEmptyList_WhenUserHasNoNotifications() {
        when(notificationRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of());

        ResponseEntity<?> response = notificationController.list(testUser);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<Notification> body = (List<Notification>) response.getBody();
        assertThat(body).isEmpty();
    }

    @Test
    void markRead_ShouldMarkNotificationAsRead_WhenExists() {
        when(notificationRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        ResponseEntity<?> response = notificationController.markRead(testUser, 1L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(testNotification.isRead()).isTrue();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void markRead_ShouldReturn404_WhenNotificationNotFound() {
        when(notificationRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        ResponseEntity<?> response = notificationController.markRead(testUser, 1L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}
