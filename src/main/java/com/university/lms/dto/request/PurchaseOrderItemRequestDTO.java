package com.university.lms.dto.request;

import java.math.BigDecimal;

public record PurchaseOrderItemRequestDTO(Long bookId, String description, int quantity, BigDecimal unitCost) {
}
