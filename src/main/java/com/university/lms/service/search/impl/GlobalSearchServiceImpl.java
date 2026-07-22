package com.university.lms.service.search.impl;

import java.util.ArrayList;
import java.util.List;

import com.university.lms.dto.request.BookSearchCriteria;
import com.university.lms.dto.request.StudentSearchCriteria;
import com.university.lms.dto.response.BookDTO;
import com.university.lms.dto.response.FacultyDTO;
import com.university.lms.dto.response.GlobalSearchResultDTO;
import com.university.lms.dto.response.StudentDTO;
import com.university.lms.service.catalog.AuthorService;
import com.university.lms.service.catalog.BookService;
import com.university.lms.service.people.FacultyService;
import com.university.lms.service.people.StudentService;
import com.university.lms.service.search.GlobalSearchService;

public final class GlobalSearchServiceImpl implements GlobalSearchService {

    private static final int PER_TYPE_LIMIT = 5;

    private final BookService bookService;
    private final AuthorService authorService;
    private final StudentService studentService;
    private final FacultyService facultyService;

    public GlobalSearchServiceImpl(BookService bookService, AuthorService authorService,
                                    StudentService studentService, FacultyService facultyService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.studentService = studentService;
        this.facultyService = facultyService;
    }

    @Override
    public List<GlobalSearchResultDTO> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String normalized = keyword.trim();
        String lowered = normalized.toLowerCase();

        List<GlobalSearchResultDTO> results = new ArrayList<>();

        BookSearchCriteria bookCriteria = BookSearchCriteria.builder().keyword(normalized).pageSize(PER_TYPE_LIMIT).build();
        for (BookDTO book : bookService.search(bookCriteria).getContent()) {
            results.add(new GlobalSearchResultDTO("BOOK", book.getId(), book.getTitle(),
                    "ISBN " + book.getIsbn() + " · " + String.join(", ", book.getAuthorNames())));
        }

        authorService.listAll().stream()
                .filter(author -> author.name() != null && author.name().toLowerCase().contains(lowered))
                .limit(PER_TYPE_LIMIT)
                .forEach(author -> results.add(new GlobalSearchResultDTO("AUTHOR", author.id(), author.name(), "Author")));

        StudentSearchCriteria studentCriteria = StudentSearchCriteria.builder().keyword(normalized).pageSize(PER_TYPE_LIMIT).build();
        for (StudentDTO student : studentService.search(studentCriteria).getContent()) {
            results.add(new GlobalSearchResultDTO("STUDENT", student.getId(), student.getUsername(),
                    "Student · " + student.getStudentId()));
        }

        facultyService.listAll().stream()
                .filter(faculty -> matchesFaculty(faculty, lowered))
                .limit(PER_TYPE_LIMIT)
                .forEach(faculty -> results.add(new GlobalSearchResultDTO("FACULTY", faculty.getId(),
                        faculty.getUsername(), "Faculty · " + faculty.getFacultyId())));

        return results;
    }

    private boolean matchesFaculty(FacultyDTO faculty, String lowered) {
        return containsIgnoreCase(faculty.getUsername(), lowered)
                || containsIgnoreCase(faculty.getFacultyId(), lowered)
                || containsIgnoreCase(faculty.getDepartment(), lowered);
    }

    private boolean containsIgnoreCase(String value, String lowered) {
        return value != null && value.toLowerCase().contains(lowered);
    }
}
