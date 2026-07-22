package com.university.lms.service.catalog;

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

import com.university.lms.dto.request.PublisherRequestDTO;
import com.university.lms.dto.response.PublisherDTO;
import com.university.lms.entity.Publisher;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.PublisherRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.catalog.impl.PublisherServiceImpl;

@ExtendWith(MockitoExtension.class)
class PublisherServiceImplTest {

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PermissionEvaluator permissionEvaluator;

    private PublisherServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PublisherServiceImpl(publisherRepository, auditLogService, new AuthContext(), permissionEvaluator);
        lenient().when(publisherRepository.save(any(Publisher.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsNewPublisher() {
        when(publisherRepository.findByName("Prentice Hall")).thenReturn(Optional.empty());

        PublisherDTO result = service.save(new PublisherRequestDTO(null, "Prentice Hall", "Addr", "555", "a@b.com"));

        assertEquals("Prentice Hall", result.name());
    }

    @Test
    void rejectsDuplicateNameOnCreate() {
        when(publisherRepository.findByName("Prentice Hall"))
                .thenReturn(Optional.of(new Publisher("Prentice Hall", null, null, null)));

        assertThrows(DuplicateResourceException.class,
                () -> service.save(new PublisherRequestDTO(null, "Prentice Hall", null, null, null)));
    }

    @Test
    void updatesExistingPublisher() {
        Publisher existing = new Publisher("Old", null, null, null);
        when(publisherRepository.findById(5L)).thenReturn(Optional.of(existing));

        PublisherDTO result = service.save(new PublisherRequestDTO(5L, "New", "Addr", "555", "a@b.com"));

        assertEquals("New", result.name());
    }

    @Test
    void updateThrowsWhenPublisherMissing() {
        when(publisherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.save(new PublisherRequestDTO(99L, "Name", null, null, null)));
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> service.save(new PublisherRequestDTO(null, " ", null, null, null)));
    }

    @Test
    void deletesPublisher() {
        Publisher publisher = new Publisher("Prentice Hall", null, null, null);
        when(publisherRepository.findById(5L)).thenReturn(Optional.of(publisher));

        service.delete(5L);

        verify(publisherRepository).delete(publisher);
    }

    @Test
    void listsAllPublishers() {
        when(publisherRepository.findAll()).thenReturn(List.of(new Publisher("A", null, null, null)));

        assertEquals(1, service.listAll().size());
    }
}
