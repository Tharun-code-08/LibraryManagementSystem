package com.university.lms.service.inventory.impl;

import java.util.List;

import com.university.lms.dto.request.SupplierRequestDTO;
import com.university.lms.dto.response.SupplierDTO;
import com.university.lms.entity.Supplier;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.SupplierRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.inventory.SupplierService;

public final class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final PermissionEvaluator permissionEvaluator;

    public SupplierServiceImpl(SupplierRepository supplierRepository, AuditLogService auditLogService,
                                AuthContext authContext, PermissionEvaluator permissionEvaluator) {
        this.supplierRepository = supplierRepository;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public SupplierDTO save(SupplierRequestDTO request) {
        permissionEvaluator.requirePermission("PROCUREMENT_MANAGE");
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Supplier name is required.");
        }

        Supplier supplier;
        if (request.id() == null) {
            supplierRepository.findByName(request.name()).ifPresent(existing -> {
                throw new DuplicateResourceException("A supplier named '" + request.name() + "' already exists.");
            });
            supplier = new Supplier(request.name(), request.contactPerson(), request.phone(), request.email(), request.address());
        } else {
            supplier = supplierRepository.findById(request.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.id()));
            supplier.setName(request.name());
            supplier.setContactPerson(request.contactPerson());
            supplier.setPhone(request.phone());
            supplier.setEmail(request.email());
            supplier.setAddress(request.address());
        }

        Supplier saved = supplierRepository.save(supplier);
        auditLogService.log(currentUserId(), request.id() == null ? "SUPPLIER_CREATED" : "SUPPLIER_UPDATED", "Supplier", saved.getId());
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        permissionEvaluator.requirePermission("PROCUREMENT_MANAGE");
        Supplier supplier = supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        supplierRepository.delete(supplier);
        auditLogService.log(currentUserId(), "SUPPLIER_DELETED", "Supplier", id);
    }

    @Override
    public List<SupplierDTO> listAll() {
        return supplierRepository.findAll().stream().map(this::toDto).toList();
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private SupplierDTO toDto(Supplier supplier) {
        return new SupplierDTO(supplier.getId(), supplier.getName(), supplier.getContactPerson(),
                supplier.getPhone(), supplier.getEmail(), supplier.getAddress());
    }
}
