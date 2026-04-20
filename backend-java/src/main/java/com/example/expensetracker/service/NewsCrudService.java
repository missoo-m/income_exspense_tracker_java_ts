package com.example.expensetracker.service;

import com.example.expensetracker.dto.NewsDto;
import com.example.expensetracker.model.News;
import com.example.expensetracker.repository.NewsRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class NewsCrudService extends AbstractCrudService<News, NewsDto> {

    public NewsCrudService(NewsRepository newsRepository) {
        super(newsRepository);
    }

    @Override
    protected News toEntity(NewsDto dto) {
        return News.builder()
                .title(dto.title())
                .content(dto.content())
                .type(dto.type())
                .author(dto.author())
                .date(Instant.now())
                .build();
    }

    @Override
    protected void updateEntity(News entity, NewsDto dto) {
        if (dto.title() != null) {
            entity.setTitle(dto.title());
        }
        if (dto.content() != null) {
            entity.setContent(dto.content());
        }
        if (dto.type() != null) {
            entity.setType(dto.type());
        }
        entity.setDate(Instant.now());
    }
}
