package com.university.lms.dto.request;

import java.math.BigDecimal;
import java.util.List;

/** Input to {@code PurchaseOrderService.create} — a new order always starts as DRAFT. */
public record PurchaseOrderRequestDTO(Long supplierId, BigDecimal budgetAmount,
                                       List<PurchaseOrderItemRequestDTO> items) {
}
