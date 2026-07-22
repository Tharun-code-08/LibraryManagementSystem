package com.university.lms.service.search;

import java.util.List;

import com.university.lms.dto.response.GlobalSearchResultDTO;

public interface GlobalSearchService {

    /** Cross-entity keyword search (books/authors/ISBN/students/faculty) for the global search
     *  overlay's instant results. @return an empty list for a null/blank keyword. */
    List<GlobalSearchResultDTO> search(String keyword);
}
