package com.university.lms.service.finance;

import java.util.Optional;

import com.university.lms.dto.request.FineSearchCriteria;
import com.university.lms.dto.request.ManualFineRequestDTO;
import com.university.lms.dto.response.FineDTO;
import com.university.lms.model.Page;

public interface FineService {

    Page<FineDTO> search(FineSearchCriteria criteria);

    Optional<FineDTO> getById(Long id);

    FineDTO createManualFine(ManualFineRequestDTO request);

    FineDTO waive(Long fineId, Long waivedByUserId);
}
