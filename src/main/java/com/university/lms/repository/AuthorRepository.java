package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Author;

public interface AuthorRepository {

    Optional<Author> findById(Long id);

    Optional<Author> findByName(String name);

    List<Author> findAll();

    Author save(Author author);

    void delete(Author author);
}
