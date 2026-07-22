package com.university.lms.service.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.request.PurchaseOrderItemRequestDTO;
import com.university.lms.dto.request.PurchaseOrderRequestDTO;
import com.university.lms.dto.response.PurchaseOrderDTO;
import com.university.lms.entity.PurchaseOrder;
import com.university.lms.entity.PurchaseOrderStatus;
import com.university.lms.entity.Supplier;
import com.university.lms.entity.User;
import com.university.lms.exception.InvalidPurchaseOrderStateException;
import com.university.lms.repository.BookRepository;
import com.university.lms.repository.PurchaseOrderRepository;
import com.university.lms.repository.SupplierRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.inventory.impl.PurchaseOrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PermissionEvaluator permissionEvaluator;

    private PurchaseOrderServiceImpl purchaseOrderService;
    private Supplier supplier;
    private User librarian;

    @BeforeEach
    void setUp() {
        purchaseOrderService = new PurchaseOrderServiceImpl(purchaseOrderRepository, supplierRepository,
                bookRepository, userRepository, auditLogService, new AuthContext(), permissionEvaluator);

        supplier = new Supplier("Acme Books", "Jane", "555-1000", "jane@acme.test", null);
        librarian = new User("librarian", "lib@library.local", "hash", null);

        lenient().when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        lenient().when(userRepository.findById(100L)).thenReturn(Optional.of(librarian));
        lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsDraftPurchaseOrderWithItems() {
        PurchaseOrderRequestDTO request = new PurchaseOrderRequestDTO(1L, BigDecimal.valueOf(500),
                List.of(new PurchaseOrderItemRequestDTO(null, "Clean Code x5", 5, BigDecimal.valueOf(40))));

        PurchaseOrderDTO result = purchaseOrderService.create(request, 100L);

        assertEquals("DRAFT", result.getStatus());
        assertEquals("Acme Books", result.getSupplierName());
        assertEquals(0, BigDecimal.valueOf(200).compareTo(result.getTotalCost()));
    }

    @Test
    void fullApprovalWorkflowTransitionsThroughEachStatus() {
        PurchaseOrder order = new PurchaseOrder(supplier, librarian, java.time.LocalDate.now(), BigDecimal.valueOf(500));
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(order));

        PurchaseOrderDTO submitted = purchaseOrderService.submitForApproval(9L);
        assertEquals("PENDING_APPROVAL", submitted.getStatus());

        PurchaseOrderDTO approved = purchaseOrderService.approve(9L, 100L);
        assertEquals("APPROVED", approved.getStatus());
        assertEquals("librarian", approved.getApprovedByName());

        PurchaseOrderDTO received = purchaseOrderService.markReceived(9L);
        assertEquals("RECEIVED", received.getStatus());
    }

    @Test
    void cannotApproveADraftOrder() {
        PurchaseOrder order = new PurchaseOrder(supplier, librarian, java.time.LocalDate.now(), BigDecimal.valueOf(500));
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(order));

        assertThrows(InvalidPurchaseOrderStateException.class, () -> purchaseOrderService.approve(9L, 100L));
    }

    @Test
    void cannotCancelAReceivedOrder() {
        PurchaseOrder order = new PurchaseOrder(supplier, librarian, java.time.LocalDate.now(), BigDecimal.valueOf(500));
        order.setStatus(PurchaseOrderStatus.RECEIVED);
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(order));

        assertThrows(InvalidPurchaseOrderStateException.class, () -> purchaseOrderService.cancel(9L));
    }
}
