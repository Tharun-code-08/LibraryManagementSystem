package com.university.lms.service.inventory;

import java.util.Optional;

import com.university.lms.dto.request.PurchaseOrderRequestDTO;
import com.university.lms.dto.request.PurchaseOrderSearchCriteria;
import com.university.lms.dto.response.PurchaseOrderDTO;
import com.university.lms.model.Page;

public interface PurchaseOrderService {

    PurchaseOrderDTO create(PurchaseOrderRequestDTO request, Long orderedByUserId);

    PurchaseOrderDTO submitForApproval(Long id);

    PurchaseOrderDTO approve(Long id, Long approvedByUserId);

    PurchaseOrderDTO cancel(Long id);

    PurchaseOrderDTO markReceived(Long id);

    Optional<PurchaseOrderDTO> getById(Long id);

    Page<PurchaseOrderDTO> search(PurchaseOrderSearchCriteria criteria);
}
