package com.university.lms.service.catalog;

import java.util.List;
import java.util.Optional;

import com.university.lms.dto.request.BookCopyCreateDTO;
import com.university.lms.dto.request.BookImportRowDTO;
import com.university.lms.dto.request.BookRequestDTO;
import com.university.lms.dto.request.BookSearchCriteria;
import com.university.lms.dto.response.BookCopyDTO;
import com.university.lms.dto.response.BookDTO;
import com.university.lms.dto.response.ImportResultDTO;
import com.university.lms.model.Page;

public interface BookService {

    BookDTO createBook(BookRequestDTO request);

    BookDTO updateBook(BookRequestDTO request);

    void deleteBook(Long id);

    void restoreBook(Long id);

    Optional<BookDTO> getById(Long id);

    Page<BookDTO> search(BookSearchCriteria criteria);

    /** Adds a new physical copy to a book, generating its barcode label image. */
    BookCopyDTO addCopy(BookCopyCreateDTO request);

    ImportResultDTO bulkImport(List<BookImportRowDTO> rows);
}
