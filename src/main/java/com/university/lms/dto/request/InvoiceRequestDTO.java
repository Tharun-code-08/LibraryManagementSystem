package com.university.lms.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceRequestDTO(Long purchaseOrderId, String invoiceNumber, LocalDate invoiceDate,
                                 BigDecimal totalAmount, String filePath) {
}
