package com.university.lms.repository;

import java.util.Optional;

import com.university.lms.dto.request.StudentSearchCriteria;
import com.university.lms.entity.Student;

public interface StudentRepository {

    Optional<Student> findById(Long id);

    Optional<Student> findByStudentId(String studentId);

    Optional<Student> findByRollNumber(String rollNumber);

    java.util.List<Student> search(StudentSearchCriteria criteria);

    long countSearchResults(StudentSearchCriteria criteria);

    Student save(Student student);
}
