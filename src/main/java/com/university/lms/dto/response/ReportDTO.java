package com.university.lms.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/** A generic tabular report ready for on-screen preview, PDF/Excel export, or printing. */
public record ReportDTO(String title, List<String> columnHeaders, List<List<String>> rows,
                        LocalDateTime generatedAt) {
}
