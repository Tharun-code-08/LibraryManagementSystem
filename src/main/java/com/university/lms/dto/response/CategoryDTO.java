package com.university.lms.dto.response;

import java.util.List;

public record CategoryDTO(Long id, String name, String description, Long parentId, List<CategoryDTO> children) {
}
