package com.university.lms.dto.request;

public record CategoryRequestDTO(Long id, String name, String description, Long parentId) {
}
