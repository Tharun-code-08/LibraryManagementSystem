package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.MembershipType;

public interface MembershipTypeRepository {

    Optional<MembershipType> findById(Long id);

    Optional<MembershipType> findByName(String name);

    List<MembershipType> findAll();

    MembershipType save(MembershipType membershipType);
}
