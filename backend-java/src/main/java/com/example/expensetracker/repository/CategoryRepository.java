package com.example.expensetracker.repository;

import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    long countByUser(User user);

    List<Category> findByUserOrderByNameAsc(User user);

    boolean existsByUserAndName(User user, String name);

    Optional<Category> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);
}