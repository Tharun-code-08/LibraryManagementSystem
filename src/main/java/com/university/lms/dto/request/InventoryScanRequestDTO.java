package com.university.lms.dto.request;

/** Input to {@code InventoryAuditService.recordScan} — one shelf-verification scan event.
 *  {@code foundStatus} is one of AVAILABLE/ISSUED/RESERVED/MAINTENANCE/LOST/RETIRED. */
public record InventoryScanRequestDTO(Long auditId, String copyBarcode, String foundStatus, String notes) {
}
