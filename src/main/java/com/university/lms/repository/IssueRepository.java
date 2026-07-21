package com.university.lms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Issue;

public interface IssueRepository {

    Optional<Issue> findById(Long id);

    /** The single OPEN (ISSUED/OVERDUE) issue for a copy, if any — enforced unique in the DB. */
    Optional<Issue> findOpenByCopyId(Long copyId);

    long countOpenByMembershipId(Long membershipId);

    List<Issue> findOverdueOpenIssues(LocalDateTime asOf);

    Issue save(Issue issue);
}
