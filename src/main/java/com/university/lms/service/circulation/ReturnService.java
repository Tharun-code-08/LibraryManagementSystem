package com.university.lms.service.circulation;

import com.university.lms.dto.request.ReturnRequestDTO;
import com.university.lms.dto.response.ReturnResultDTO;

public interface ReturnService {

    ReturnResultDTO returnBook(ReturnRequestDTO request, Long receivedByUserId);
}
