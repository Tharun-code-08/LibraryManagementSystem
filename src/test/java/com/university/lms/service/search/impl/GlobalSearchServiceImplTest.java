package com.university.lms.service.search.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.response.AuthorDTO;
import com.university.lms.dto.response.BookDTO;
import com.university.lms.dto.response.FacultyDTO;
import com.university.lms.dto.response.GlobalSearchResultDTO;
import com.university.lms.dto.response.StudentDTO;
import com.university.lms.model.Page;
import com.university.lms.service.catalog.AuthorService;
import com.university.lms.service.catalog.BookService;
import com.university.lms.service.people.FacultyService;
import com.university.lms.service.people.StudentService;

@ExtendWith(MockitoExtension.class)
class GlobalSearchServiceImplTest {

    @Mock
    private BookService bookService;

    @Mock
    private AuthorService authorService;

    @Mock
    private StudentService studentService;

    @Mock
    private FacultyService facultyService;

    private GlobalSearchServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GlobalSearchServiceImpl(bookService, authorService, studentService, facultyService);
    }

    @Test
    void blankKeywordReturnsEmptyWithoutQuerying() {
        List<GlobalSearchResultDTO> result = service.search("   ");

        assertTrue(result.isEmpty());
        verifyNoInteractions(bookService, authorService, studentService, facultyService);
    }

    @Test
    void searchCombinesResultsAcrossEntityTypes() {
        BookDTO book = BookDTO.builder().id(1L).isbn("978-1").title("Clean Code")
                .authorNames(Set.of("Robert Martin")).build();
        when(bookService.search(any())).thenReturn(new Page<>(List.of(book), 0, 5, 1));

        AuthorDTO author = new AuthorDTO(2L, "Robert Martin", null, null);
        when(authorService.listAll()).thenReturn(List.of(author));

        when(studentService.search(any())).thenReturn(new Page<>(List.of(), 0, 5, 0));

        FacultyDTO faculty = FacultyDTO.builder().id(4L).username("adas").facultyId("F1").department("CS").build();
        when(facultyService.listAll()).thenReturn(List.of(faculty));

        List<GlobalSearchResultDTO> result = service.search("robert");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.entityType().equals("BOOK") && r.id().equals(1L)));
        assertTrue(result.stream().anyMatch(r -> r.entityType().equals("AUTHOR") && r.id().equals(2L)));
        assertTrue(result.stream().noneMatch(r -> r.entityType().equals("FACULTY")));
    }

    @Test
    void searchMatchesFacultyByDepartment() {
        when(bookService.search(any())).thenReturn(new Page<>(List.of(), 0, 5, 0));
        when(authorService.listAll()).thenReturn(List.of());
        when(studentService.search(any())).thenReturn(new Page<>(List.of(), 0, 5, 0));
        FacultyDTO faculty = FacultyDTO.builder().id(4L).username("adas").facultyId("F1").department("Computer Science").build();
        when(facultyService.listAll()).thenReturn(List.of(faculty));

        List<GlobalSearchResultDTO> result = service.search("computer");

        assertEquals(1, result.size());
        assertEquals("FACULTY", result.get(0).entityType());
    }
}
