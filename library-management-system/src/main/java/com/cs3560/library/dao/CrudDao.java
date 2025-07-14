package com.cs3560.library.dao;

import java.util.List;
import java.util.Optional;

public interface CrudDao<T> {
    T save(T entity);
    Optional<T> findById(Object id);
    List<T> findAll();
    T update(T entity);
    void delete(T entity);
    void deleteById(Object id);
} 