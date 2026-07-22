package com.university.lms.repository;

import java.util.List;

import com.university.lms.entity.InventoryAuditItem;

public interface InventoryAuditItemRepository {

    List<InventoryAuditItem> findByAuditId(Long auditId);

    InventoryAuditItem save(InventoryAuditItem item);
}
