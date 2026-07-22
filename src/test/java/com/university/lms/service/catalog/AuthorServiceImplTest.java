package com.university.lms.service.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.request.AuthorRequestDTO;
import com.university.lms.dto.response.AuthorDTO;
import com.university.lms.entity.Author;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.AuthorRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.catalog.impl.AuthorServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuditLogService auditLogService;

    private AuthorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthorServiceImpl(authorRepository, auditLogService, new AuthContext());
        lenient().when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsNewAuthor() {
        when(authorRepository.findByName("Robert Martin")).thenReturn(Optional.empty());

        AuthorDTO result = service.save(new AuthorRequestDTO(null, "Robert Martin", "Bio", "US"));

        assertEquals("Robert Martin", result.name());
    }

    @Test
    void rejectsDuplicateNameOnCreate() {
        when(authorRepository.findByName("Robert Martin")).thenReturn(Optional.of(new Author("Robert Martin", null, null)));

        assertThrows(DuplicateResourceException.class,
                () -> service.save(new AuthorRequestDTO(null, "Robert Martin", null, null)));
    }

    @Test
    void updatesExistingAuthor() {
        Author existing = new Author("Old Name", null, null);
        when(authorRepository.findById(5L)).thenReturn(Optional.of(existing));

        AuthorDTO result = service.save(new AuthorRequestDTO(5L, "New Name", "Bio", "UK"));

        assertEquals("New Name", result.name());
        assertEquals("UK", result.nationality());
    }

    @Test
    void updateThrowsWhenAuthorMissing() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.save(new AuthorRequestDTO(99L, "Name", null, null)));
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> service.save(new AuthorRequestDTO(null, "  ", null, null)));
    }

    @Test
    void deletesAuthor() {
        Author author = new Author("Robert Martin", null, null);
        when(authorRepository.findById(5L)).thenReturn(Optional.of(author));

        service.delete(5L);

        org.mockito.Mockito.verify(authorRepository).delete(author);
    }

    @Test
    void deleteThrowsWhenAuthorMissing() {
        when(authorRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(404L));
    }

    @Test
    void listsAllAuthors() {
        when(authorRepository.findAll()).thenReturn(List.of(new Author("A", null, null), new Author("B", null, null)));

        List<AuthorDTO> result = service.listAll();

        assertEquals(2, result.size());
    }
}
