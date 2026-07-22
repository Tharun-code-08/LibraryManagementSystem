package com.university.lms.repository;

import java.util.List;

import com.university.lms.entity.Permission;

/** Persistence contract for {@link Permission}. */
public interface PermissionRepository {

    List<Permission> findAll();
}
