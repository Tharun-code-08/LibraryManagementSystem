package com.university.lms.service.inventory.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.university.lms.dto.request.InventoryScanRequestDTO;
import com.university.lms.dto.response.InventoryAuditDTO;
import com.university.lms.dto.response.InventoryAuditItemDTO;
import com.university.lms.entity.Branch;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.entity.InventoryAudit;
import com.university.lms.entity.InventoryAuditItem;
import com.university.lms.entity.InventoryAuditStatus;
import com.university.lms.entity.User;
import com.university.lms.exception.BookCopyNotFoundException;
import com.university.lms.exception.InvalidInventoryAuditStateException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.BookCopyRepository;
import com.university.lms.repository.BranchRepository;
import com.university.lms.repository.InventoryAuditItemRepository;
import com.university.lms.repository.InventoryAuditRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.inventory.InventoryAuditService;

public final class InventoryAuditServiceImpl implements InventoryAuditService {

    private final InventoryAuditRepository inventoryAuditRepository;
    private final InventoryAuditItemRepository inventoryAuditItemRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final PermissionEvaluator permissionEvaluator;

    public InventoryAuditServiceImpl(InventoryAuditRepository inventoryAuditRepository,
                                      InventoryAuditItemRepository inventoryAuditItemRepository,
                                      BookCopyRepository bookCopyRepository, BranchRepository branchRepository,
                                      UserRepository userRepository, AuditLogService auditLogService,
                                      AuthContext authContext, PermissionEvaluator permissionEvaluator) {
        this.inventoryAuditRepository = inventoryAuditRepository;
        this.inventoryAuditItemRepository = inventoryAuditItemRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public InventoryAuditDTO startAudit(Long branchId, Long conductedByUserId) {
        permissionEvaluator.requirePermission("INVENTORY_MANAGE");
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
        User conductedBy = userRepository.findById(conductedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", conductedByUserId));

        InventoryAudit audit = new InventoryAudit(branch, conductedBy, LocalDateTime.now());
        InventoryAudit saved = inventoryAuditRepository.save(audit);
        auditLogService.log(currentUserId(), "INVENTORY_AUDIT_STARTED", "InventoryAudit", saved.getId());
        return toDto(saved, List.of());
    }

    @Override
    public InventoryAuditDTO recordScan(InventoryScanRequestDTO request) {
        permissionEvaluator.requirePermission("INVENTORY_MANAGE");
        InventoryAudit audit = inventoryAuditRepository.findById(request.auditId())
                .orElseThrow(() -> new ResourceNotFoundException("InventoryAudit", request.auditId()));
        if (audit.getStatus() != InventoryAuditStatus.IN_PROGRESS) {
            throw new InvalidInventoryAuditStateException("This audit is not in progress.");
        }

        BookCopy copy = bookCopyRepository.findByBarcode(request.copyBarcode())
                .orElseThrow(() -> new BookCopyNotFoundException(request.copyBarcode()));

        String expectedStatus = copy.getStatus().name();
        String foundStatus = request.foundStatus();
        InventoryAuditItem item = new InventoryAuditItem(audit, copy, expectedStatus, foundStatus, request.notes());
        inventoryAuditItemRepository.save(item);

        if (!expectedStatus.equals(foundStatus)) {
            copy.setStatus(BookCopyStatus.valueOf(foundStatus));
            bookCopyRepository.save(copy);
            auditLogService.log(currentUserId(), "INVENTORY_DISCREPANCY_CORRECTED", "BookCopy", copy.getId());
        }

        auditLogService.log(currentUserId(), "INVENTORY_SCAN_RECORDED", "InventoryAudit", audit.getId());
        return toDto(audit, inventoryAuditItemRepository.findByAuditId(audit.getId()));
    }

    @Override
    public InventoryAuditDTO completeAudit(Long auditId) {
        permissionEvaluator.requirePermission("INVENTORY_MANAGE");
        InventoryAudit audit = inventoryAuditRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryAudit", auditId));
        if (audit.getStatus() != InventoryAuditStatus.IN_PROGRESS) {
            throw new InvalidInventoryAuditStateException("This audit is not in progress.");
        }
        audit.setStatus(InventoryAuditStatus.COMPLETED);
        audit.setCompletedAt(LocalDateTime.now());
        InventoryAudit saved = inventoryAuditRepository.save(audit);
        auditLogService.log(currentUserId(), "INVENTORY_AUDIT_COMPLETED", "InventoryAudit", saved.getId());
        return toDto(saved, inventoryAuditItemRepository.findByAuditId(saved.getId()));
    }

    @Override
    public Optional<InventoryAuditDTO> getById(Long auditId) {
        return inventoryAuditRepository.findById(auditId)
                .map(audit -> toDto(audit, inventoryAuditItemRepository.findByAuditId(auditId)));
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private InventoryAuditDTO toDto(InventoryAudit audit, List<InventoryAuditItem> items) {
        List<InventoryAuditItemDTO> itemDtos = items.stream()
                .map(item -> new InventoryAuditItemDTO(item.getId(), item.getBookCopy().getBarcode(),
                        item.getBookCopy().getBook().getTitle(), item.getExpectedStatus(), item.getFoundStatus(), item.getNotes()))
                .toList();

        return new InventoryAuditDTO(audit.getId(), audit.getBranch().getName(), audit.getConductedBy().getUsername(),
                audit.getStartedAt(), audit.getCompletedAt(), audit.getStatus().name(), itemDtos);
    }
}
