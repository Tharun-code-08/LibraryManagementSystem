package com.university.lms.service.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.inventory.impl.InvoiceServiceImpl;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private AuditLogService auditLogService;

    private InvoiceServiceImpl service;
    private PurchaseOrder purchaseOrder;

    @BeforeEach
    void setUp() {
        service = new InvoiceServiceImpl(invoiceRepository, purchaseOrderRepository, auditLogService, new AuthContext());
        purchaseOrder = new PurchaseOrder(null, null, LocalDate.now(), BigDecimal.valueOf(500));
        lenient().when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void recordsInvoiceForApprovedOrder() {
        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(invoiceRepository.findByInvoiceNumber("INV-1")).thenReturn(Optional.empty());

        InvoiceDTO result = service.recordInvoice(
                new InvoiceRequestDTO(1L, "INV-1", LocalDate.now(), BigDecimal.valueOf(500), "/path.pdf"));

        assertEquals("INV-1", result.invoiceNumber());
    }

    @Test
    void rejectsInvoiceForDraftOrder() {
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        assertThrows(InvalidPurchaseOrderStateException.class, () -> service.recordInvoice(
                new InvoiceRequestDTO(1L, "INV-1", LocalDate.now(), BigDecimal.valueOf(500), "/path.pdf")));
    }

    @Test
    void rejectsDuplicateInvoiceNumber() {
        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(invoiceRepository.findByInvoiceNumber("INV-1"))
                .thenReturn(Optional.of(new Invoice(purchaseOrder, "INV-1", LocalDate.now(), BigDecimal.TEN, null)));

        assertThrows(DuplicateResourceException.class, () -> service.recordInvoice(
                new InvoiceRequestDTO(1L, "INV-1", LocalDate.now(), BigDecimal.valueOf(500), "/path.pdf")));
    }

    @Test
    void throwsWhenPurchaseOrderMissing() {
        when(purchaseOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.recordInvoice(
                new InvoiceRequestDTO(99L, "INV-1", LocalDate.now(), BigDecimal.valueOf(500), null)));
    }

    @Test
    void listsInvoicesForPurchaseOrder() {
        when(invoiceRepository.findByPurchaseOrderId(1L))
                .thenReturn(List.of(new Invoice(purchaseOrder, "INV-1", LocalDate.now(), BigDecimal.TEN, null)));

        List<InvoiceDTO> result = service.listForPurchaseOrder(1L);

        assertEquals(1, result.size());
    }
}
