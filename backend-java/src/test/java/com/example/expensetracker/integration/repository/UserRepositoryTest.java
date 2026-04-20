package com.example.expensetracker.integration.repository;


import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .fullName("Иван Иванов")
                .email("ivan@example.com")
                .password("hashed_password")
                .role(User.Role.NORMAL)
                .build();
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        Optional<User> found = userRepository.findByEmail("ivan@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Иван Иванов");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    void save_ShouldPersistUser_WhenDataIsValid() {
        User newUser = User.builder()
                .fullName("Петр Петров")
                .email("petr@example.com")
                .password("hashed_password2")
                .role(User.Role.NORMAL)
                .build();

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFullName()).isEqualTo("Петр Петров");
    }

    @Test
    void existsByRole_ShouldReturnTrue_WhenAdminExists() {
        User admin = User.builder()
                .fullName("Администратор")
                .email("admin@example.com")
                .password("hashed_password")
                .role(User.Role.ADMIN)
                .build();
        userRepository.save(admin);

        boolean exists = userRepository.existsByRole(User.Role.ADMIN);
        assertThat(exists).isTrue();
    }

    @Test
    void delete_ShouldRemoveUser_WhenUserExists() {
        Long userId = testUser.getId();
        userRepository.deleteById(userId);

        Optional<User> found = userRepository.findById(userId);
        assertThat(found).isEmpty();
    }
}