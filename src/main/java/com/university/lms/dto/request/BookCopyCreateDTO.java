package com.university.lms.dto.request;

public record BookCopyCreateDTO(Long bookId, Long branchId, String shelf, String rack, String rowLabel) {
}
