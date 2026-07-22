package com.university.lms.service.inventory;

import java.util.List;

import com.university.lms.dto.request.InvoiceRequestDTO;
import com.university.lms.dto.response.InvoiceDTO;

public interface InvoiceService {

    InvoiceDTO recordInvoice(InvoiceRequestDTO request);

    List<InvoiceDTO> listForPurchaseOrder(Long purchaseOrderId);
}
