package com.university.lms.service.inventory.impl;

import java.util.List;

import com.university.lms.dto.request.InvoiceRequestDTO;
import com.university.lms.dto.response.InvoiceDTO;
import com.university.lms.entity.Invoice;
import com.university.lms.entity.PurchaseOrder;
import com.university.lms.entity.PurchaseOrderStatus;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.InvalidPurchaseOrderStateException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.InvoiceRepository;
import com.university.lms.repository.PurchaseOrderRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.inventory.InvoiceService;

public final class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final PermissionEvaluator permissionEvaluator;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, PurchaseOrderRepository purchaseOrderRepository,
                               AuditLogService auditLogService, AuthContext authContext,
                               PermissionEvaluator permissionEvaluator) {
        this.invoiceRepository = invoiceRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public InvoiceDTO recordInvoice(InvoiceRequestDTO request) {
        permissionEvaluator.requirePermission("PROCUREMENT_MANAGE");
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(request.purchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", request.purchaseOrderId()));
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.APPROVED && purchaseOrder.getStatus() != PurchaseOrderStatus.RECEIVED) {
            throw new InvalidPurchaseOrderStateException("Invoices can only be recorded for approved or received purchase orders.");
        }
        invoiceRepository.findByInvoiceNumber(request.invoiceNumber()).ifPresent(existing -> {
            throw new DuplicateResourceException("An invoice numbered '" + request.invoiceNumber() + "' already exists.");
        });

        Invoice invoice = new Invoice(purchaseOrder, request.invoiceNumber(), request.invoiceDate(),
                request.totalAmount(), request.filePath());
        Invoice saved = invoiceRepository.save(invoice);
        auditLogService.log(currentUserId(), "INVOICE_RECORDED", "Invoice", saved.getId());
        return toDto(saved);
    }

    @Override
    public List<InvoiceDTO> listForPurchaseOrder(Long purchaseOrderId) {
        return invoiceRepository.findByPurchaseOrderId(purchaseOrderId).stream().map(this::toDto).toList();
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private InvoiceDTO toDto(Invoice invoice) {
        return new InvoiceDTO(invoice.getId(), invoice.getPurchaseOrder().getId(), invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(), invoice.getTotalAmount(), invoice.getFilePath());
    }
}
