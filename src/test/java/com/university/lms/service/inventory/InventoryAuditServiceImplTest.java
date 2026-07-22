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

import com.university.lms.dto.request.InventoryScanRequestDTO;
import com.university.lms.dto.response.InventoryAuditDTO;
import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyCondition;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.entity.Branch;
import com.university.lms.entity.InventoryAudit;
import com.university.lms.entity.InventoryAuditItem;
import com.university.lms.entity.InventoryAuditStatus;
import com.university.lms.entity.User;
import com.university.lms.exception.InvalidInventoryAuditStateException;
import com.university.lms.repository.BookCopyRepository;
import com.university.lms.repository.BranchRepository;
import com.university.lms.repository.InventoryAuditItemRepository;
import com.university.lms.repository.InventoryAuditRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.inventory.impl.InventoryAuditServiceImpl;

@ExtendWith(MockitoExtension.class)
class InventoryAuditServiceImplTest {

    @Mock
    private InventoryAuditRepository inventoryAuditRepository;

    @Mock
    private InventoryAuditItemRepository inventoryAuditItemRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private InventoryAuditServiceImpl auditService;
    private InventoryAudit audit;
    private BookCopy copy;

    @BeforeEach
    void setUp() {
        auditService = new InventoryAuditServiceImpl(inventoryAuditRepository, inventoryAuditItemRepository,
                bookCopyRepository, branchRepository, userRepository, auditLogService, new AuthContext());

        Branch branch = new Branch("Main Campus", "MAIN", null, null);
        User librarian = new User("librarian", "lib@library.local", "hash", null);
        Book book = new Book("978-1", "Clean Code", null, null, null, null, null, null, BigDecimal.valueOf(50), null, null);
        copy = new BookCopy(book, branch, "BC100", null, null, null, BookCopyCondition.GOOD, null);
        audit = new InventoryAudit(branch, librarian, java.time.LocalDateTime.now());

        lenient().when(inventoryAuditRepository.save(any(InventoryAudit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(inventoryAuditItemRepository.save(any(InventoryAuditItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(bookCopyRepository.save(any(BookCopy.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void recordingAMismatchedScanCorrectsTheCopyStatus() {
        when(inventoryAuditRepository.findById(1L)).thenReturn(Optional.of(audit));
        when(bookCopyRepository.findByBarcode("BC100")).thenReturn(Optional.of(copy));
        when(inventoryAuditItemRepository.findByAuditId(null)).thenReturn(List.of());

        InventoryAuditDTO result = auditService.recordScan(new InventoryScanRequestDTO(1L, "BC100", "LOST", "Not on shelf"));

        assertEquals(BookCopyStatus.LOST, copy.getStatus());
        assertEquals("Main Campus", result.branchName());
    }

    @Test
    void cannotScanAgainstACompletedAudit() {
        audit.setStatus(InventoryAuditStatus.COMPLETED);
        when(inventoryAuditRepository.findById(1L)).thenReturn(Optional.of(audit));

        assertThrows(InvalidInventoryAuditStateException.class,
                () -> auditService.recordScan(new InventoryScanRequestDTO(1L, "BC100", "LOST", null)));
    }

    @Test
    void completingAnAuditSetsCompletedStatusAndTimestamp() {
        when(inventoryAuditRepository.findById(1L)).thenReturn(Optional.of(audit));
        when(inventoryAuditItemRepository.findByAuditId(null)).thenReturn(List.of());

        InventoryAuditDTO result = auditService.completeAudit(1L);

        assertEquals("COMPLETED", result.status());
    }
}
