package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Role;

/** Persistence contract for {@link Role}. */
public interface RoleRepository {

    Optional<Role> findByName(String name);

    Optional<Role> findById(Long id);

    List<Role> findAll();

    Role save(Role role);
}
