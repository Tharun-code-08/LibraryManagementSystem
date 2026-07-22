package com.university.lms.dto.response;

import java.util.List;

public record ImportResultDTO(int successCount, List<RejectedRow> rejectedRows) {

    public record RejectedRow(int rowNumber, String reason) {
    }
}
