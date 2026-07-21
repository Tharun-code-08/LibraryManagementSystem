package com.university.lms.service.catalog;

import java.util.List;

import com.university.lms.dto.request.CategoryRequestDTO;
import com.university.lms.dto.response.CategoryDTO;

public interface CategoryService {

    CategoryDTO save(CategoryRequestDTO request);

    void delete(Long id);

    /** Root categories with their full nested subtree attached. */
    List<CategoryDTO> listTree();
}
