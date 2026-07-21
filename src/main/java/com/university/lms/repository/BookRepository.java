package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.dto.request.BookSearchCriteria;
import com.university.lms.entity.Book;

public interface BookRepository {

    Optional<Book> findById(Long id);

    Optional<Book> findByIsbn(String isbn);

    List<Book> search(BookSearchCriteria criteria);

    long countSearchResults(BookSearchCriteria criteria);

    Book save(Book book);
}
