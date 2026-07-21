package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Invoice;

public interface InvoiceRepository {

    List<Invoice> findByPurchaseOrderId(Long purchaseOrderId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Invoice save(Invoice invoice);
}
