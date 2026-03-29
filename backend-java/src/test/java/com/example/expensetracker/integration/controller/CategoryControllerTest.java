package com.example.expensetracker.integration.controller;


import com.example.expensetracker.controller.CategoryController;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.CategoryRepository;
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
class CategoryControllerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryController categoryController;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("Тестовый Пользователь")
                .email("test@example.com")
                .role(User.Role.NORMAL)
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Еда")
                .user(testUser)
                .isDefault(false)
                .build();
    }

    @Test
    void getAll_ShouldReturnCategories_WhenUserHasCategories() {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.countByUser(testUser)).thenReturn(1L);
        when(categoryRepository.findByUserOrderByNameAsc(testUser)).thenReturn(categories);

        ResponseEntity<?> response = categoryController.getAll(testUser);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<Category> body = (List<Category>) response.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).getName()).isEqualTo("Еда");
    }

    @Test
    void getAll_ShouldCreateDefaultCategories_WhenUserHasNoCategories() {
        when(categoryRepository.countByUser(testUser)).thenReturn(0L);
        when(categoryRepository.findByUserOrderByNameAsc(testUser)).thenReturn(List.of());

        ResponseEntity<?> response = categoryController.getAll(testUser);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(categoryRepository, atLeastOnce()).save(any(Category.class));
    }

    @Test
    void create_ShouldCreateCategory_WhenNameIsValid() {
        CategoryController.CategoryRequest request = new CategoryController.CategoryRequest("Новая категория");
        
        when(categoryRepository.existsByUserAndName(eq(testUser), eq("Новая категория"))).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        ResponseEntity<?> response = categoryController.create(testUser, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Category body = (Category) response.getBody();
        assertThat(body.getName()).isEqualTo("Новая категория");
    }

    @Test
    void create_ShouldReturn400_WhenNameIsEmpty() {
        CategoryController.CategoryRequest request = new CategoryController.CategoryRequest("");

        ResponseEntity<?> response = categoryController.create(testUser, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void create_ShouldReturn400_WhenNameAlreadyExists() {
        CategoryController.CategoryRequest request = new CategoryController.CategoryRequest("Еда");
        
        when(categoryRepository.existsByUserAndName(testUser, "Еда")).thenReturn(true);

        ResponseEntity<?> response = categoryController.create(testUser, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void update_ShouldUpdateCategory_WhenNameIsValid() {
        CategoryController.CategoryRequest request = new CategoryController.CategoryRequest("Обновленная категория");
        
        when(categoryRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByUserAndName(testUser, "Обновленная категория")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        ResponseEntity<?> response = categoryController.update(testUser, 1L, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void update_ShouldReturn404_WhenCategoryNotFound() {
        CategoryController.CategoryRequest request = new CategoryController.CategoryRequest("Обновленная категория");
        
        when(categoryRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        ResponseEntity<?> response = categoryController.update(testUser, 1L, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void delete_ShouldDeleteCategory_WhenExists() {
        when(categoryRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testCategory));

        ResponseEntity<?> response = categoryController.delete(testUser, 1L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void delete_ShouldReturn404_WhenCategoryNotFound() {
        when(categoryRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        ResponseEntity<?> response = categoryController.delete(testUser, 1L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}
