package com.university.lms.service.people;

import java.util.List;
import java.util.Optional;

import com.university.lms.dto.request.StudentImportRowDTO;
import com.university.lms.dto.request.StudentRegistrationRequestDTO;
import com.university.lms.dto.request.StudentSearchCriteria;
import com.university.lms.dto.response.ImportResultDTO;
import com.university.lms.dto.response.StudentDTO;
import com.university.lms.model.Page;

public interface StudentService {

    StudentDTO register(StudentRegistrationRequestDTO request);

    StudentDTO update(StudentRegistrationRequestDTO request);

    void changeStatus(Long studentId, String status);

    Optional<StudentDTO> getById(Long id);

    Page<StudentDTO> search(StudentSearchCriteria criteria);

    ImportResultDTO bulkImport(List<StudentImportRowDTO> rows);
}
