package com.university.lms.service.catalog.impl;

import java.util.List;

import com.university.lms.dto.request.AuthorRequestDTO;
import com.university.lms.dto.response.AuthorDTO;
import com.university.lms.entity.Author;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.AuthorRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.catalog.AuthorService;

public final class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final PermissionEvaluator permissionEvaluator;

    public AuthorServiceImpl(AuthorRepository authorRepository, AuditLogService auditLogService,
                              AuthContext authContext, PermissionEvaluator permissionEvaluator) {
        this.authorRepository = authorRepository;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public AuthorDTO save(AuthorRequestDTO request) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Author name is required.");
        }

        Author author;
        if (request.id() == null) {
            authorRepository.findByName(request.name()).ifPresent(existing -> {
                throw new DuplicateResourceException("An author named '" + request.name() + "' already exists.");
            });
            author = new Author(request.name(), request.biography(), request.nationality());
        } else {
            author = authorRepository.findById(request.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Author", request.id()));
            author.setName(request.name());
            author.setBiography(request.biography());
            author.setNationality(request.nationality());
        }

        Author saved = authorRepository.save(author);
        auditLogService.log(currentUserId(), request.id() == null ? "AUTHOR_CREATED" : "AUTHOR_UPDATED", "Author", saved.getId());
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        Author author = authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Author", id));
        authorRepository.delete(author);
        auditLogService.log(currentUserId(), "AUTHOR_DELETED", "Author", id);
    }

    @Override
    public List<AuthorDTO> listAll() {
        return authorRepository.findAll().stream().map(this::toDto).toList();
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private AuthorDTO toDto(Author author) {
        return new AuthorDTO(author.getId(), author.getName(), author.getBiography(), author.getNationality());
    }
}
