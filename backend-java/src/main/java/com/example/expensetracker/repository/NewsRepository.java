package com.example.expensetracker.repository;

import com.example.expensetracker.model.News;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findByTypeOrderByDateDesc(String type);

    void deleteByAuthor(User author);
}

