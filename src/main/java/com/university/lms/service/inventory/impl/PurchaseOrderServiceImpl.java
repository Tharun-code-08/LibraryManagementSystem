package com.university.lms.service.inventory.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.university.lms.dto.request.PurchaseOrderItemRequestDTO;
import com.university.lms.dto.request.PurchaseOrderRequestDTO;
import com.university.lms.dto.request.PurchaseOrderSearchCriteria;
import com.university.lms.dto.response.PurchaseOrderDTO;
import com.university.lms.dto.response.PurchaseOrderItemDTO;
import com.university.lms.entity.Book;
import com.university.lms.entity.PurchaseOrder;
import com.university.lms.entity.PurchaseOrderItem;
import com.university.lms.entity.PurchaseOrderStatus;
import com.university.lms.entity.Supplier;
import com.university.lms.entity.User;
import com.university.lms.exception.InvalidPurchaseOrderStateException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.model.Page;
import com.university.lms.repository.BookRepository;
import com.university.lms.repository.PurchaseOrderRepository;
import com.university.lms.repository.SupplierRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.inventory.PurchaseOrderService;

public final class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;

    public PurchaseOrderServiceImpl(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository,
                                     BookRepository bookRepository, UserRepository userRepository,
                                     AuditLogService auditLogService, AuthContext authContext) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
    }

    @Override
    public PurchaseOrderDTO create(PurchaseOrderRequestDTO request, Long orderedByUserId) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("A purchase order must have at least one line item.");
        }
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()));
        User orderedBy = userRepository.findById(orderedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", orderedByUserId));

        PurchaseOrder purchaseOrder = new PurchaseOrder(supplier, orderedBy, LocalDate.now(), request.budgetAmount());
        for (PurchaseOrderItemRequestDTO itemRequest : request.items()) {
            Book book = itemRequest.bookId() != null
                    ? bookRepository.findById(itemRequest.bookId())
                            .orElseThrow(() -> new ResourceNotFoundException("Book", itemRequest.bookId()))
                    : null;
            purchaseOrder.addItem(new PurchaseOrderItem(book, itemRequest.description(), itemRequest.quantity(), itemRequest.unitCost()));
        }

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        auditLogService.log(currentUserId(), "PURCHASE_ORDER_CREATED", "PurchaseOrder", saved.getId());
        return toDto(saved);
    }

    @Override
    public PurchaseOrderDTO submitForApproval(Long id) {
        PurchaseOrder purchaseOrder = requireStatus(id, PurchaseOrderStatus.DRAFT, "submitted for approval");
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        auditLogService.log(currentUserId(), "PURCHASE_ORDER_SUBMITTED", "PurchaseOrder", saved.getId());
        return toDto(saved);
    }

    @Override
    public PurchaseOrderDTO approve(Long id, Long approvedByUserId) {
        PurchaseOrder purchaseOrder = requireStatus(id, PurchaseOrderStatus.PENDING_APPROVAL, "approved");
        User approvedBy = userRepository.findById(approvedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", approvedByUserId));
        purchaseOrder.setApprovedBy(approvedBy);
        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        auditLogService.log(approvedByUserId, "PURCHASE_ORDER_APPROVED", "PurchaseOrder", saved.getId());
        return toDto(saved);
    }

    @Override
    public PurchaseOrderDTO cancel(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        if (purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new InvalidPurchaseOrderStateException("A received purchase order cannot be cancelled.");
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        auditLogService.log(currentUserId(), "PURCHASE_ORDER_CANCELLED", "PurchaseOrder", saved.getId());
        return toDto(saved);
    }

    @Override
    public PurchaseOrderDTO markReceived(Long id) {
        PurchaseOrder purchaseOrder = requireStatus(id, PurchaseOrderStatus.APPROVED, "marked as received");
        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        auditLogService.log(currentUserId(), "PURCHASE_ORDER_RECEIVED", "PurchaseOrder", saved.getId());
        return toDto(saved);
    }

    @Override
    public Optional<PurchaseOrderDTO> getById(Long id) {
        return purchaseOrderRepository.findById(id).map(this::toDto);
    }

    @Override
    public Page<PurchaseOrderDTO> search(PurchaseOrderSearchCriteria criteria) {
        List<PurchaseOrderDTO> content = purchaseOrderRepository.search(criteria).stream().map(this::toDto).toList();
        long total = purchaseOrderRepository.countSearchResults(criteria);
        return new Page<>(content, criteria.getPageNumber(), criteria.getPageSize(), total);
    }

    private PurchaseOrder requireStatus(Long id, PurchaseOrderStatus required, String actionDescription) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        if (purchaseOrder.getStatus() != required) {
            throw new InvalidPurchaseOrderStateException(
                    "Only a " + required.name() + " purchase order can be " + actionDescription + ".");
        }
        return purchaseOrder;
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private PurchaseOrderDTO toDto(PurchaseOrder purchaseOrder) {
        List<PurchaseOrderItemDTO> items = purchaseOrder.getItems().stream()
                .map(item -> new PurchaseOrderItemDTO(item.getId(), item.getDescription(), item.getQuantity(),
                        item.getUnitCost(), item.getLineTotal()))
                .toList();

        return PurchaseOrderDTO.builder()
                .id(purchaseOrder.getId())
                .supplierName(purchaseOrder.getSupplier().getName())
                .orderedByName(purchaseOrder.getOrderedBy().getUsername())
                .orderDate(purchaseOrder.getOrderDate())
                .status(purchaseOrder.getStatus().name())
                .budgetAmount(purchaseOrder.getBudgetAmount())
                .approvedByName(purchaseOrder.getApprovedBy() != null ? purchaseOrder.getApprovedBy().getUsername() : null)
                .items(items)
                .totalCost(purchaseOrder.getTotalCost())
                .build();
    }
}
