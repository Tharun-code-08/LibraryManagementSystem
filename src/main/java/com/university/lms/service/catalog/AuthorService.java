package com.university.lms.service.catalog;

import java.util.List;

import com.university.lms.dto.request.AuthorRequestDTO;
import com.university.lms.dto.response.AuthorDTO;

public interface AuthorService {

    AuthorDTO save(AuthorRequestDTO request);

    void delete(Long id);

    List<AuthorDTO> listAll();
}
