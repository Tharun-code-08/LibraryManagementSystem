package com.university.lms.service.catalog;

import java.util.List;

import com.university.lms.dto.request.PublisherRequestDTO;
import com.university.lms.dto.response.PublisherDTO;

public interface PublisherService {

    PublisherDTO save(PublisherRequestDTO request);

    void delete(Long id);

    List<PublisherDTO> listAll();
}
