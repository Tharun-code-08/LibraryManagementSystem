package com.university.lms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceDTO(Long id, Long purchaseOrderId, String invoiceNumber, LocalDate invoiceDate,
                          BigDecimal totalAmount, String filePath) {
}
