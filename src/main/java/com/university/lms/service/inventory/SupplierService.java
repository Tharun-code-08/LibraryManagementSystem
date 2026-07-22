package com.university.lms.service.inventory;

import java.util.List;

import com.university.lms.dto.request.SupplierRequestDTO;
import com.university.lms.dto.response.SupplierDTO;

public interface SupplierService {

    SupplierDTO save(SupplierRequestDTO request);

    void delete(Long id);

    List<SupplierDTO> listAll();
}
