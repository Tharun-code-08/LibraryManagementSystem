package com.university.lms.dto.response;

import java.time.LocalDateTime;

public record IssueResultDTO(Long issueId, String bookTitle, String copyBarcode, String memberName,
                              String memberIdentifier, LocalDateTime issueDate, LocalDateTime dueDate) {
}
