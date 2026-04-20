package com.example.expensetracker.integration.controller;

import com.example.expensetracker.controller.AdminController;
import com.example.expensetracker.model.News;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private AdminController adminController;

    private User adminUser;
    private User normalUser;
    private News testNews;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .fullName("Администратор")
                .email("admin@example.com")
                .role(User.Role.ADMIN)
                .build();

        normalUser = User.builder()
                .id(2L)
                .fullName("Обычный Пользователь")
                .email("user@example.com")
                .role(User.Role.NORMAL)
                .build();

        testNews = News.builder()
                .id(1L)
                .title("Новость")
                .content("Содержание новости")
                .type("news")
                .author(adminUser)
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnUsers_WhenAdmin() {
        List<User> users = Arrays.asList(adminUser, normalUser);
        when(userRepository.findAll()).thenReturn(users);

        ResponseEntity<?> response = adminController.getAllUsers(adminUser, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<User> body = (List<User>) response.getBody();
        assertThat(body).hasSize(2);
    }

    @Test
    void getAllUsers_ShouldReturn403_WhenNotAdmin() {
        ResponseEntity<?> response = adminController.getAllUsers(normalUser, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenAdminAndUserExists() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        doNothing().when(notificationRepository).deleteByUser(normalUser);
        doNothing().when(budgetRepository).deleteByUser(normalUser);
        doNothing().when(expenseRepository).deleteByUser(normalUser);
        doNothing().when(incomeRepository).deleteByUser(normalUser);
        doNothing().when(categoryRepository).deleteByUser(normalUser);
        doNothing().when(newsRepository).deleteByAuthor(normalUser);
        doNothing().when(userRepository).delete(normalUser);

        ResponseEntity<?> response = adminController.deleteUser(adminUser, 2L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(userRepository).delete(normalUser);
    }

    @Test
    void deleteUser_ShouldReturn404_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = adminController.deleteUser(adminUser, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void addNews_ShouldAddNews_WhenAdmin() {
        AdminController.NewsRequest request = new AdminController.NewsRequest(
                "Новая новость", "Содержание", "news"
        );

        when(newsRepository.save(any(News.class))).thenAnswer(invocation -> {
            News saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        ResponseEntity<?> response = adminController.addNews(adminUser, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(newsRepository).save(any(News.class));
    }

    @Test
    void addNews_ShouldReturn400_WhenFieldsMissing() {
        AdminController.NewsRequest request = new AdminController.NewsRequest(null, null, null);

        ResponseEntity<?> response = adminController.addNews(adminUser, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void deleteNews_ShouldDeleteNews_WhenAdminAndNewsExists() {
        when(newsRepository.findById(1L)).thenReturn(Optional.of(testNews));
        doNothing().when(newsRepository).delete(testNews);

        ResponseEntity<?> response = adminController.deleteNews(adminUser, 1L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(newsRepository).delete(testNews);
    }

}
