package com.university.lms.service.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.request.BookImportRowDTO;
import com.university.lms.dto.request.BookRequestDTO;
import com.university.lms.dto.response.BookDTO;
import com.university.lms.dto.response.ImportResultDTO;
import com.university.lms.entity.Book;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ValidationException;
import com.university.lms.repository.AuthorRepository;
import com.university.lms.repository.BookCopyRepository;
import com.university.lms.repository.BookRepository;
import com.university.lms.repository.BranchRepository;
import com.university.lms.repository.CategoryRepository;
import com.university.lms.repository.PublisherRepository;
import com.university.lms.repository.TagRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.catalog.impl.BookServiceImpl;
import com.university.lms.util.BarcodeGenerator;
import com.university.lms.util.QrCodeGenerator;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PermissionEvaluator permissionEvaluator;

    @TempDir
    private Path tempDir;

    private BookServiceImpl bookService;

    @BeforeEach
    void setUp() {
        BarcodeGenerator barcodeGenerator = new BarcodeGenerator(tempDir.resolve("barcodes"));
        QrCodeGenerator qrCodeGenerator = new QrCodeGenerator(tempDir.resolve("qrcodes"));
        AuthContext authContext = new AuthContext();

        bookService = new BookServiceImpl(
                bookRepository, bookCopyRepository, authorRepository, publisherRepository,
                categoryRepository, tagRepository, branchRepository, barcodeGenerator,
                qrCodeGenerator, auditLogService, authContext, permissionEvaluator);

        lenient().when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(bookCopyRepository.countByBookId(any())).thenReturn(0L);
        lenient().when(bookCopyRepository.countByBookIdAndStatus(any(), any())).thenReturn(0L);
        lenient().when(authorRepository.findByName(any())).thenReturn(Optional.empty());
        lenient().when(authorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createBookSucceedsForValidNewIsbn() {
        when(bookRepository.findByIsbn("978-3-16-148410-0")).thenReturn(Optional.empty());

        BookRequestDTO request = BookRequestDTO.builder()
                .isbn("978-3-16-148410-0")
                .title("Clean Code")
                .cost(BigDecimal.TEN)
                .build();

        BookDTO result = bookService.createBook(request);

        assertEquals("Clean Code", result.getTitle());
        assertEquals("978-3-16-148410-0", result.getIsbn());
    }

    @Test
    void createBookRejectsDuplicateIsbn() {
        when(bookRepository.findByIsbn("978-3-16-148410-0"))
                .thenReturn(Optional.of(new Book("978-3-16-148410-0", "Existing", null, null, null, null, null, null, BigDecimal.ZERO, null, null)));

        BookRequestDTO request = BookRequestDTO.builder()
                .isbn("978-3-16-148410-0")
                .title("New Title")
                .build();

        assertThrows(DuplicateResourceException.class, () -> bookService.createBook(request));
    }

    @Test
    void createBookRejectsMissingTitle() {
        BookRequestDTO request = BookRequestDTO.builder().isbn("978-3-16-148410-0").build();
        assertThrows(ValidationException.class, () -> bookService.createBook(request));
    }

    @Test
    void bulkImportReportsRejectedRowsSeparatelyFromSuccesses() {
        when(bookRepository.findByIsbn("111")).thenReturn(Optional.empty());
        when(bookRepository.findByIsbn("222")).thenReturn(
                Optional.of(new Book("222", "Duplicate", null, null, null, null, null, null, BigDecimal.ZERO, null, null)));

        List<BookImportRowDTO> rows = List.of(
                new BookImportRowDTO(2, "111", "Valid Book", "Author One", null, null, null, null, BigDecimal.ZERO),
                new BookImportRowDTO(3, "222", "Duplicate Book", "Author Two", null, null, null, null, BigDecimal.ZERO));

        ImportResultDTO result = bookService.bulkImport(rows);

        assertEquals(1, result.successCount());
        assertEquals(1, result.rejectedRows().size());
        assertEquals(3, result.rejectedRows().get(0).rowNumber());
    }
}
