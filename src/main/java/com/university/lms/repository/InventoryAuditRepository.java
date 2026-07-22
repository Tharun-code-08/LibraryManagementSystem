package com.university.lms.repository;

import java.util.Optional;

import com.university.lms.entity.InventoryAudit;

public interface InventoryAuditRepository {

    Optional<InventoryAudit> findById(Long id);

    InventoryAudit save(InventoryAudit inventoryAudit);
}
