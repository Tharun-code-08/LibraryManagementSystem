package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.dto.request.FineSearchCriteria;
import com.university.lms.entity.Fine;

public interface FineRepository {

    Optional<Fine> findById(Long id);

    List<Fine> search(FineSearchCriteria criteria);

    long countSearchResults(FineSearchCriteria criteria);

    Fine save(Fine fine);
}
