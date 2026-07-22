package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Category;

public interface CategoryRepository {

    Optional<Category> findById(Long id);

    Optional<Category> findByNameAndParent(String name, Long parentId);

    List<Category> findRootCategories();

    List<Category> findAll();

    Category save(Category category);

    void delete(Category category);
}
