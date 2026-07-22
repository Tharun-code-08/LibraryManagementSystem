package com.university.lms.service.catalog.impl;

import java.util.List;

import com.university.lms.dto.request.PublisherRequestDTO;
import com.university.lms.dto.response.PublisherDTO;
import com.university.lms.entity.Publisher;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.PublisherRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.catalog.PublisherService;

public final class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final PermissionEvaluator permissionEvaluator;

    public PublisherServiceImpl(PublisherRepository publisherRepository, AuditLogService auditLogService,
                                 AuthContext authContext, PermissionEvaluator permissionEvaluator) {
        this.publisherRepository = publisherRepository;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public PublisherDTO save(PublisherRequestDTO request) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Publisher name is required.");
        }

        Publisher publisher;
        if (request.id() == null) {
            publisherRepository.findByName(request.name()).ifPresent(existing -> {
                throw new DuplicateResourceException("A publisher named '" + request.name() + "' already exists.");
            });
            publisher = new Publisher(request.name(), request.address(), request.phone(), request.email());
        } else {
            publisher = publisherRepository.findById(request.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Publisher", request.id()));
            publisher.setName(request.name());
            publisher.setAddress(request.address());
            publisher.setPhone(request.phone());
            publisher.setEmail(request.email());
        }

        Publisher saved = publisherRepository.save(publisher);
        auditLogService.log(currentUserId(), request.id() == null ? "PUBLISHER_CREATED" : "PUBLISHER_UPDATED", "Publisher", saved.getId());
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        Publisher publisher = publisherRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Publisher", id));
        publisherRepository.delete(publisher);
        auditLogService.log(currentUserId(), "PUBLISHER_DELETED", "Publisher", id);
    }

    @Override
    public List<PublisherDTO> listAll() {
        return publisherRepository.findAll().stream().map(this::toDto).toList();
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private PublisherDTO toDto(Publisher publisher) {
        return new PublisherDTO(publisher.getId(), publisher.getName(), publisher.getAddress(), publisher.getPhone(), publisher.getEmail());
    }
}
