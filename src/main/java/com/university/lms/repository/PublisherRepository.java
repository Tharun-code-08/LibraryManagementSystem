package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Publisher;

public interface PublisherRepository {

    Optional<Publisher> findById(Long id);

    Optional<Publisher> findByName(String name);

    List<Publisher> findAll();

    Publisher save(Publisher publisher);

    void delete(Publisher publisher);
}
