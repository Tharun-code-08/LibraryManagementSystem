package com.university.lms.dto.response;

import java.math.BigDecimal;

public record PurchaseOrderItemDTO(Long id, String description, int quantity, BigDecimal unitCost, BigDecimal lineTotal) {
}
