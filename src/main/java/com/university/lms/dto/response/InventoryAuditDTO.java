package com.university.lms.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record InventoryAuditDTO(Long id, String branchName, String conductedByName, LocalDateTime startedAt,
                                 LocalDateTime completedAt, String status, List<InventoryAuditItemDTO> items) {
}
