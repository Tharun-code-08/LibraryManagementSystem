package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.dto.request.PurchaseOrderSearchCriteria;
import com.university.lms.entity.PurchaseOrder;

public interface PurchaseOrderRepository {

    Optional<PurchaseOrder> findById(Long id);

    List<PurchaseOrder> search(PurchaseOrderSearchCriteria criteria);

    long countSearchResults(PurchaseOrderSearchCriteria criteria);

    PurchaseOrder save(PurchaseOrder purchaseOrder);
}
