package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Branch;

public interface BranchRepository {

    Optional<Branch> findById(Long id);

    List<Branch> findAll();
}
