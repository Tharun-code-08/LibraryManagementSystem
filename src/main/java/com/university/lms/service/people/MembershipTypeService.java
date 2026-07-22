package com.university.lms.service.people;

import java.util.List;

import com.university.lms.dto.request.MembershipTypeRequestDTO;
import com.university.lms.dto.response.MembershipTypeDTO;

public interface MembershipTypeService {

    MembershipTypeDTO save(MembershipTypeRequestDTO request);

    List<MembershipTypeDTO> listAll();
}
