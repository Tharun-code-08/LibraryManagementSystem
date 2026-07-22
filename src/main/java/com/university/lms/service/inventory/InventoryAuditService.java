package com.university.lms.service.inventory;

import java.util.Optional;

import com.university.lms.dto.request.InventoryScanRequestDTO;
import com.university.lms.dto.response.InventoryAuditDTO;

public interface InventoryAuditService {

    InventoryAuditDTO startAudit(Long branchId, Long conductedByUserId);

    /** Records one shelf-scan event, correcting the copy's system status if it disagrees. */
    InventoryAuditDTO recordScan(InventoryScanRequestDTO request);

    InventoryAuditDTO completeAudit(Long auditId);

    Optional<InventoryAuditDTO> getById(Long auditId);
}
