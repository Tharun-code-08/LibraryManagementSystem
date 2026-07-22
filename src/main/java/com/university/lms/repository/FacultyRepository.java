package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Faculty;

public interface FacultyRepository {

    Optional<Faculty> findById(Long id);

    Optional<Faculty> findByFacultyId(String facultyId);

    List<Faculty> findAll();

    Faculty save(Faculty faculty);
}
