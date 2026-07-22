package com.university.lms.repository;

import java.util.Optional;

import com.university.lms.entity.Tag;

public interface TagRepository {

    Optional<Tag> findByName(String name);

    Tag save(Tag tag);
}
