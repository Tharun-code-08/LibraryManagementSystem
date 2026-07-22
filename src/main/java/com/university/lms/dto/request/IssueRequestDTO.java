package com.university.lms.dto.request;

/** Input to {@code IssueService.issueBook} — both fields are scanned or typed at the desk. */
public record IssueRequestDTO(String memberIdentifier, String copyBarcode) {
}
