package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.User;

/** Persistence contract for {@link User}. Services depend only on this interface. */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    List<User> findAll();

    User save(User user);
}
