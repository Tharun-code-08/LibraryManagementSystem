package com.university.lms.dto.request;

/** Input to {@code ReturnService.returnBook}. {@code condition} is one of GOOD/DAMAGED/LOST. */
public record ReturnRequestDTO(String copyBarcode, String condition, String notes) {
}
