package com.university.lms.service.circulation;

import com.university.lms.dto.request.IssueRequestDTO;
import com.university.lms.dto.response.IssueResultDTO;

public interface IssueService {

    IssueResultDTO issueBook(IssueRequestDTO request, Long issuedByUserId);
}
