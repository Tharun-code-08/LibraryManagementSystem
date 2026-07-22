package com.university.lms.dto.response;

public record BookCopyDTO(Long id, Long bookId, String barcode, String shelf, String rack, String rowLabel,
                           String condition, String status, String barcodeImagePath) {
}
