package com.university.lms.dto.response;

public record InventoryAuditItemDTO(Long id, String copyBarcode, String bookTitle,
                                     String expectedStatus, String foundStatus, String notes) {
}
