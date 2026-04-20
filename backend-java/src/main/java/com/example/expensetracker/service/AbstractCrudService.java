package com.example.expensetracker.service;

import com.example.expensetracker.aop.Loggable;
import com.example.expensetracker.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public abstract class AbstractCrudService<Entity, DTO> {

    private final JpaRepository<Entity, Long> repository;

    protected AbstractCrudService(JpaRepository<Entity, Long> repository) {
        this.repository = repository;
    }

    protected abstract Entity toEntity(DTO dto);

    protected abstract void updateEntity(Entity entity, DTO dto);

    @Loggable("create entity")
    public Entity create(DTO dto) {
        Entity entity = toEntity(dto);
        return repository.save(entity);
    }

    @Loggable("get all entities")
    public List<Entity> findAll() {
        return repository.findAll();
    }

    @Loggable("get entity by id")
    public Entity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сущность не найдена: " + id));
    }

    @Loggable("update entity")
    public Entity update(Long id, DTO dto) {
        Entity existing = findById(id);
        updateEntity(existing, dto);
        return repository.save(existing);
    }

    @Loggable("delete entity")
    public void deleteById(Long id) {
        Entity existing = findById(id);
        repository.delete(existing);
    }
}
