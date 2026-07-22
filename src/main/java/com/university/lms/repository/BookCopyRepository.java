package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyStatus;

public interface BookCopyRepository {

    Optional<BookCopy> findById(Long id);

    Optional<BookCopy> findByBarcode(String barcode);

    List<BookCopy> findByBookId(Long bookId);

    long countByBookId(Long bookId);

    long countByBookIdAndStatus(Long bookId, BookCopyStatus status);

    List<BookCopy> findAll();

    List<BookCopy> findByStatus(BookCopyStatus status);

    BookCopy save(BookCopy bookCopy);
}
