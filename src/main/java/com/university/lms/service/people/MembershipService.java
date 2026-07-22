package com.university.lms.service.people;

import com.university.lms.dto.response.MembershipDTO;
import com.university.lms.entity.HolderType;

public interface MembershipService {

    /** Creates a fresh membership, or extends the holder's current active one if present. */
    MembershipDTO assignOrRenew(HolderType holderType, Long holderId, Long membershipTypeId, int validityDays);

    java.util.Optional<MembershipDTO> getActiveMembership(HolderType holderType, Long holderId);
}
