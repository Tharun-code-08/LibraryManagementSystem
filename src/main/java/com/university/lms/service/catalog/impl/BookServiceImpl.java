package com.university.lms.service.catalog.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.university.lms.dto.request.BookCopyCreateDTO;
import com.university.lms.dto.request.BookImportRowDTO;
import com.university.lms.dto.request.BookRequestDTO;
import com.university.lms.dto.request.BookSearchCriteria;
import com.university.lms.dto.response.BookCopyDTO;
import com.university.lms.dto.response.BookDTO;
import com.university.lms.dto.response.ImportResultDTO;
import com.university.lms.entity.Author;
import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyCondition;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.entity.Branch;
import com.university.lms.entity.Category;
import com.university.lms.entity.Publisher;
import com.university.lms.entity.Tag;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.exception.ValidationException;
import com.university.lms.model.Page;
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
import com.university.lms.service.catalog.BookService;
import com.university.lms.util.BarcodeGenerator;
import com.university.lms.util.QrCodeGenerator;
import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.Validator;
import com.university.lms.validation.impl.BookValidator;

public final class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final BranchRepository branchRepository;
    private final BarcodeGenerator barcodeGenerator;
    private final QrCodeGenerator qrCodeGenerator;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final PermissionEvaluator permissionEvaluator;

    private final Validator<BookRequestDTO> bookValidator = new BookValidator();

    public BookServiceImpl(BookRepository bookRepository, BookCopyRepository bookCopyRepository,
                            AuthorRepository authorRepository, PublisherRepository publisherRepository,
                            CategoryRepository categoryRepository, TagRepository tagRepository,
                            BranchRepository branchRepository, BarcodeGenerator barcodeGenerator,
                            QrCodeGenerator qrCodeGenerator, AuditLogService auditLogService,
                            AuthContext authContext, PermissionEvaluator permissionEvaluator) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.branchRepository = branchRepository;
        this.barcodeGenerator = barcodeGenerator;
        this.qrCodeGenerator = qrCodeGenerator;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public BookDTO createBook(BookRequestDTO request) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        validate(request);
        bookRepository.findByIsbn(request.getIsbn()).ifPresent(existing -> {
            throw new DuplicateResourceException("A book with ISBN " + request.getIsbn() + " already exists.");
        });

        Book book = new Book(request.getIsbn(), request.getTitle(), request.getSubtitle(), request.getEdition(),
                request.getVolume(), request.getLanguage(), resolvePublisher(request.getPublisherId()),
                resolveCategory(request.getCategoryId()), request.getCost() == null ? BigDecimal.ZERO : request.getCost(),
                request.getPurchaseDate(), request.getVendor());
        book.setCoverImagePath(request.getCoverImagePath());
        book.getAuthors().addAll(resolveAuthors(request.getAuthorIds()));
        book.getTags().addAll(resolveOrCreateTags(request.getTagNames()));

        Book saved = bookRepository.save(book);
        generateAndAttachQrCode(saved);

        auditLogService.log(currentUserId(), "BOOK_CREATED", "Book", saved.getId());
        return toDto(saved);
    }

    @Override
    public BookDTO updateBook(BookRequestDTO request) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        if (request.getId() == null) {
            throw new IllegalArgumentException("Book id is required for an update.");
        }
        validate(request);

        Book book = bookRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", request.getId()));

        bookRepository.findByIsbn(request.getIsbn())
                .filter(other -> !other.getId().equals(request.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("A book with ISBN " + request.getIsbn() + " already exists.");
                });

        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setSubtitle(request.getSubtitle());
        book.setEdition(request.getEdition());
        book.setVolume(request.getVolume());
        book.setLanguage(request.getLanguage());
        book.setPublisher(resolvePublisher(request.getPublisherId()));
        book.setCategory(resolveCategory(request.getCategoryId()));
        book.setCost(request.getCost() == null ? BigDecimal.ZERO : request.getCost());
        book.setPurchaseDate(request.getPurchaseDate());
        book.setVendor(request.getVendor());
        book.setCoverImagePath(request.getCoverImagePath());

        book.getAuthors().clear();
        book.getAuthors().addAll(resolveAuthors(request.getAuthorIds()));
        book.getTags().clear();
        book.getTags().addAll(resolveOrCreateTags(request.getTagNames()));

        Book saved = bookRepository.save(book);
        auditLogService.log(currentUserId(), "BOOK_UPDATED", "Book", saved.getId());
        return toDto(saved);
    }

    @Override
    public void deleteBook(Long id) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        Book book = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book", id));
        book.setDeleted(true);
        bookRepository.save(book);
        auditLogService.log(currentUserId(), "BOOK_DELETED", "Book", id);
    }

    @Override
    public void restoreBook(Long id) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        Book book = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book", id));
        book.setDeleted(false);
        bookRepository.save(book);
        auditLogService.log(currentUserId(), "BOOK_RESTORED", "Book", id);
    }

    @Override
    public Optional<BookDTO> getById(Long id) {
        return bookRepository.findById(id).map(this::toDto);
    }

    @Override
    public Page<BookDTO> search(BookSearchCriteria criteria) {
        List<BookDTO> content = bookRepository.search(criteria).stream().map(this::toDto).toList();
        long total = bookRepository.countSearchResults(criteria);
        return new Page<>(content, criteria.getPageNumber(), criteria.getPageSize(), total);
    }

    @Override
    public BookCopyDTO addCopy(BookCopyCreateDTO request) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", request.bookId()));
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.branchId()));

        String barcodeValue = "BC" + book.getId() + "-" + System.currentTimeMillis();
        BookCopy copy = new BookCopy(book, branch, barcodeValue, request.shelf(), request.rack(),
                request.rowLabel(), BookCopyCondition.NEW, LocalDate.now());
        BookCopy saved = bookCopyRepository.save(copy);

        String imagePath = barcodeGenerator.generate(barcodeValue, "copy-" + saved.getId());
        auditLogService.log(currentUserId(), "BOOK_COPY_ADDED", "BookCopy", saved.getId());

        return new BookCopyDTO(saved.getId(), book.getId(), saved.getBarcode(), saved.getShelf(), saved.getRack(),
                saved.getRowLabel(), saved.getCondition().name(), saved.getStatus().name(), imagePath);
    }

    @Override
    public ImportResultDTO bulkImport(List<BookImportRowDTO> rows) {
        permissionEvaluator.requirePermission("BOOK_MANAGE");
        int successCount = 0;
        List<ImportResultDTO.RejectedRow> rejectedRows = new ArrayList<>();

        for (BookImportRowDTO row : rows) {
            try {
                importRow(row);
                successCount++;
            } catch (Exception e) {
                rejectedRows.add(new ImportResultDTO.RejectedRow(row.rowNumber(), e.getMessage()));
            }
        }
        return new ImportResultDTO(successCount, rejectedRows);
    }

    private void importRow(BookImportRowDTO row) {
        if (row.title() == null || row.title().isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (row.isbn() == null || row.isbn().isBlank()) {
            throw new IllegalArgumentException("ISBN is required.");
        }
        bookRepository.findByIsbn(row.isbn()).ifPresent(existing -> {
            throw new DuplicateResourceException("A book with ISBN " + row.isbn() + " already exists.");
        });

        Publisher publisher = row.publisherName() == null || row.publisherName().isBlank()
                ? null : findOrCreatePublisherByName(row.publisherName());
        Category category = row.categoryName() == null || row.categoryName().isBlank()
                ? null : findOrCreateRootCategoryByName(row.categoryName());

        Book book = new Book(row.isbn(), row.title(), null, row.edition(), null, row.language(),
                publisher, category, row.cost() == null ? BigDecimal.ZERO : row.cost(), LocalDate.now(), null);
        book.getAuthors().addAll(findOrCreateAuthorsByNames(row.authorNames()));

        Book saved = bookRepository.save(book);
        generateAndAttachQrCode(saved);
        auditLogService.log(currentUserId(), "BOOK_IMPORTED", "Book", saved.getId());
    }

    private void generateAndAttachQrCode(Book book) {
        String qrPath = qrCodeGenerator.generate(book.getIsbn() + "|" + book.getTitle(), "book-" + book.getId());
        book.setQrCodePath(qrPath);
        book.setBarcodeValue(book.getIsbn());
        bookRepository.save(book);
    }

    private void validate(BookRequestDTO request) {
        ValidationResult result = bookValidator.validate(request);
        if (!result.isValid()) {
            throw new ValidationException(result.getErrors());
        }
    }

    private Publisher resolvePublisher(Long publisherId) {
        if (publisherId == null) {
            return null;
        }
        return publisherRepository.findById(publisherId)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher", publisherId));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }

    private Set<Author> resolveAuthors(Set<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return Set.of();
        }
        Set<Author> authors = new HashSet<>();
        for (Long authorId : authorIds) {
            authors.add(authorRepository.findById(authorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Author", authorId)));
        }
        return authors;
    }

    private Set<Tag> resolveOrCreateTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Set.of();
        }
        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            tags.add(tagRepository.findByName(name).orElseGet(() -> tagRepository.save(new Tag(name))));
        }
        return tags;
    }

    private Publisher findOrCreatePublisherByName(String name) {
        return publisherRepository.findByName(name.trim())
                .orElseGet(() -> publisherRepository.save(new Publisher(name.trim(), null, null, null)));
    }

    private Category findOrCreateRootCategoryByName(String name) {
        return categoryRepository.findByNameAndParent(name.trim(), null)
                .orElseGet(() -> categoryRepository.save(new Category(name.trim(), null, null)));
    }

    private Set<Author> findOrCreateAuthorsByNames(String semicolonSeparatedNames) {
        if (semicolonSeparatedNames == null || semicolonSeparatedNames.isBlank()) {
            return Set.of();
        }
        Set<Author> authors = new HashSet<>();
        for (String name : semicolonSeparatedNames.split(";")) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            authors.add(authorRepository.findByName(trimmed)
                    .orElseGet(() -> authorRepository.save(new Author(trimmed, null, null))));
        }
        return authors;
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private BookDTO toDto(Book book) {
        long totalCopies = bookCopyRepository.countByBookId(book.getId());
        long availableCopies = bookCopyRepository.countByBookIdAndStatus(book.getId(), BookCopyStatus.AVAILABLE);

        return BookDTO.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .subtitle(book.getSubtitle())
                .edition(book.getEdition())
                .volume(book.getVolume())
                .language(book.getLanguage())
                .publisherName(book.getPublisher() != null ? book.getPublisher().getName() : null)
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .authorNames(book.getAuthors().stream().map(Author::getName).collect(Collectors.toUnmodifiableSet()))
                .tagNames(book.getTags().stream().map(Tag::getName).collect(Collectors.toUnmodifiableSet()))
                .cost(book.getCost())
                .purchaseDate(book.getPurchaseDate())
                .vendor(book.getVendor())
                .coverImagePath(book.getCoverImagePath())
                .qrCodePath(book.getQrCodePath())
                .deleted(book.isDeleted())
                .totalCopies((int) totalCopies)
                .availableCopies((int) availableCopies)
                .build();
    }
}
