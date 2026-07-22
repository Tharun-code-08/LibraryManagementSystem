package com.university.lms.service.people;

import java.util.List;
import java.util.Optional;

import com.university.lms.dto.request.FacultyRegistrationRequestDTO;
import com.university.lms.dto.response.FacultyDTO;

public interface FacultyService {

    FacultyDTO register(FacultyRegistrationRequestDTO request);

    FacultyDTO update(FacultyRegistrationRequestDTO request);

    Optional<FacultyDTO> getById(Long id);

    List<FacultyDTO> listAll();
}
