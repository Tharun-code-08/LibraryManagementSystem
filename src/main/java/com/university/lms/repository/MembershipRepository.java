package com.university.lms.repository;

import java.util.Optional;

import com.university.lms.entity.HolderType;
import com.university.lms.entity.Membership;

public interface MembershipRepository {

    Optional<Membership> findActiveByHolder(HolderType holderType, Long holderId);

    Membership save(Membership membership);
}
