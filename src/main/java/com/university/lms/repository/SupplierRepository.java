package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Supplier;

public interface SupplierRepository {

    Optional<Supplier> findById(Long id);

    Optional<Supplier> findByName(String name);

    List<Supplier> findAll();

    Supplier save(Supplier supplier);

    void delete(Supplier supplier);
}
