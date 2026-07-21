package com.university.lms.repository;

import java.util.Optional;

import com.university.lms.entity.Role;

/** Persistence contract for {@link Role}. */
public interface RoleRepository {

    Optional<Role> findByName(String name);
}
