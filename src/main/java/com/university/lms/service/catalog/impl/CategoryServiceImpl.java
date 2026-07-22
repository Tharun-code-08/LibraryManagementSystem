package com.university.lms.service.catalog.impl;

import java.util.List;
import java.util.Objects;

import com.university.lms.dto.request.CategoryRequestDTO;
import com.university.lms.dto.response.CategoryDTO;
import com.university.lms.entity.Category;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.CategoryRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.catalog.CategoryService;

public final class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final PermissionEvaluator permissionEvaluator;

    public CategoryServiceImpl(CategoryRepository categoryRepository, AuditLogService auditLogService,
                                AuthContext authContext, PermissionEvaluator permissionEvaluator) {
        this.categoryRepository = categoryRepository;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public CategoryDTO save(CategoryRequestDTO request) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Category name is required.");
        }

        Category parent = request.parentId() != null
                ? categoryRepository.findById(request.parentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category", request.parentId()))
                : null;

        Category category;
        if (request.id() == null) {
            categoryRepository.findByNameAndParent(request.name(), request.parentId()).ifPresent(existing -> {
                throw new DuplicateResourceException("A category named '" + request.name() + "' already exists at this level.");
            });
            category = new Category(request.name(), parent, request.description());
        } else {
            category = categoryRepository.findById(request.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.id()));
            category.setName(request.name());
            category.setDescription(request.description());
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        auditLogService.log(currentUserId(), request.id() == null ? "CATEGORY_CREATED" : "CATEGORY_UPDATED", "Category", saved.getId());
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id));
        boolean hasChildren = categoryRepository.findAll().stream().anyMatch(c -> Objects.equals(parentIdOf(c), id));
        if (hasChildren) {
            throw new IllegalStateException("Cannot delete a category that still has sub-categories.");
        }
        categoryRepository.delete(category);
        auditLogService.log(currentUserId(), "CATEGORY_DELETED", "Category", id);
    }

    @Override
    public List<CategoryDTO> listTree() {
        List<Category> all = categoryRepository.findAll();
        return buildTree(all, null);
    }

    private List<CategoryDTO> buildTree(List<Category> all, Long parentId) {
        return all.stream()
                .filter(category -> Objects.equals(parentIdOf(category), parentId))
                .map(category -> new CategoryDTO(category.getId(), category.getName(), category.getDescription(),
                        parentId, buildTree(all, category.getId())))
                .toList();
    }

    /** Reads only the parent's identifier, which is safe on an uninitialized lazy proxy. */
    private Long parentIdOf(Category category) {
        return category.getParent() != null ? category.getParent().getId() : null;
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private CategoryDTO toDto(Category category) {
        return new CategoryDTO(category.getId(), category.getName(), category.getDescription(), parentIdOf(category), List.of());
    }
}
