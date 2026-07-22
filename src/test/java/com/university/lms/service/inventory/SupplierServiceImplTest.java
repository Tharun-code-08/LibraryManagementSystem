package com.university.lms.service.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.request.SupplierRequestDTO;
import com.university.lms.dto.response.SupplierDTO;
import com.university.lms.entity.Supplier;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.SupplierRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.inventory.impl.SupplierServiceImpl;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private AuditLogService auditLogService;

    private SupplierServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SupplierServiceImpl(supplierRepository, auditLogService, new AuthContext());
        lenient().when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsNewSupplier() {
        when(supplierRepository.findByName("Acme Books")).thenReturn(Optional.empty());

        SupplierDTO result = service.save(new SupplierRequestDTO(null, "Acme Books", "Jane", "555", "a@b.com", "Addr"));

        assertEquals("Acme Books", result.name());
    }

    @Test
    void rejectsDuplicateNameOnCreate() {
        when(supplierRepository.findByName("Acme Books"))
                .thenReturn(Optional.of(new Supplier("Acme Books", null, null, null, null)));

        assertThrows(DuplicateResourceException.class,
                () -> service.save(new SupplierRequestDTO(null, "Acme Books", null, null, null, null)));
    }

    @Test
    void updatesExistingSupplier() {
        Supplier existing = new Supplier("Old", null, null, null, null);
        when(supplierRepository.findById(5L)).thenReturn(Optional.of(existing));

        SupplierDTO result = service.save(new SupplierRequestDTO(5L, "New", "Jane", "555", "a@b.com", "Addr"));

        assertEquals("New", result.name());
    }

    @Test
    void updateThrowsWhenSupplierMissing() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.save(new SupplierRequestDTO(99L, "Name", null, null, null, null)));
    }

    @Test
    void deletesSupplier() {
        Supplier supplier = new Supplier("Acme Books", null, null, null, null);
        when(supplierRepository.findById(5L)).thenReturn(Optional.of(supplier));

        service.delete(5L);

        verify(supplierRepository).delete(supplier);
    }

    @Test
    void listsAllSuppliers() {
        when(supplierRepository.findAll()).thenReturn(List.of(new Supplier("A", null, null, null, null)));

        assertEquals(1, service.listAll().size());
    }
}
